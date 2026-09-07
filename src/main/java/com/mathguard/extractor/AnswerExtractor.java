package com.mathguard.extractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the most likely committed numerical answer from an AI response.
 *
 * v1.0 heuristics:
 * 1. choose the latest explicit answer marker ("Final answer" or "Answer")
 * 2. within an explicit answer sentence, prefer the right-hand side of an equals sign
 * 3. otherwise choose the latest equals-sign/result-phrase candidate
 * 4. otherwise accept a fallback only when the response contains exactly one numeric token
 *
 * The extractor intentionally returns empty for ambiguous or malformed unmarked responses
 * rather than silently choosing a partial number.
 */
public final class AnswerExtractor {

    /**
     * Liberal numeric token used for extraction. Strict format validation is deliberately
     * deferred to AnswerNormalizer, but token boundaries prevent partial extraction from
     * unsupported forms such as 1e3, 1.2.3, or 1/2/3.
     */
    private static final String NUMBER_BODY =
            "[-+]?(?:(?:\\d[\\d,]*(?:\\.\\d+)?)|(?:\\.\\d+))" +
            "(?:\\s*/\\s*[-+]?(?:(?:\\d[\\d,]*(?:\\.\\d+)?)|(?:\\.\\d+)))?" +
            "(?:\\s*%)?";

    private static final String NUMBER =
            "(?<![\\w.,/%])(" + NUMBER_BODY + ")(?![\\w.,/%])";

    private static final Pattern FINAL_ANSWER_MARKER = Pattern.compile(
            "(?i)\\bfinal\\s+answer\\b\\s*(?:is|=|:|-)?\\s*");

    private static final Pattern ANSWER_MARKER = Pattern.compile(
            "(?i)\\banswer\\b\\s*(?:is|=|:|-)?\\s*");

    private static final Pattern EQUALS_SIGN = Pattern.compile("=\\s*" + NUMBER);

    private static final Pattern RESULT_PHRASE = Pattern.compile(
            "(?i)\\b(?:gives?|equals?|results?\\s+in|result\\s+is)\\b[^\\d+\\-]{0,20}" + NUMBER);

    private static final Pattern FALLBACK_NUMBER = Pattern.compile(NUMBER);

    public Optional<String> extract(String response) {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        // AI text frequently uses the mathematical Unicode minus sign.
        String text = response.replace('\u2212', '-');

        Optional<String> explicit = latestExplicitCandidate(text);
        if (explicit.isPresent()) {
            return explicit;
        }

        Optional<String> secondary = latestSecondaryCandidate(text);
        if (secondary.isPresent()) {
            return secondary;
        }

        return singleFallbackNumber(text);
    }

    private Optional<String> latestExplicitCandidate(String text) {
        List<Candidate> candidates = new ArrayList<>();
        collectMarkerCandidates(FINAL_ANSWER_MARKER, text, candidates);
        collectMarkerCandidates(ANSWER_MARKER, text, candidates);

        return candidates.stream()
                .max(Comparator.comparingInt(Candidate::position))
                .map(Candidate::value);
    }

    private void collectMarkerCandidates(Pattern markerPattern, String text, List<Candidate> candidates) {
        Matcher marker = markerPattern.matcher(text);
        while (marker.find()) {
            String segment = answerSentence(text, marker.end());
            Optional<String> value = valueFromAnswerSegment(segment);
            value.ifPresent(v -> candidates.add(new Candidate(marker.start(), v)));
        }
    }

    /**
     * Takes only the sentence/line immediately following an explicit answer marker.
     * This prevents a later check sentence from overriding a committed answer.
     */
    private String answerSentence(String text, int start) {
        int limit = Math.min(text.length(), start + 180);
        int end = limit;

        for (int i = start; i < limit; i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r' || c == '!' || c == '?') {
                end = i;
                break;
            }
            if (c == '.') {
                boolean decimalPoint = i > 0 && i + 1 < text.length()
                        && Character.isDigit(text.charAt(i - 1))
                        && Character.isDigit(text.charAt(i + 1));
                if (!decimalPoint) {
                    end = i;
                    break;
                }
            }
        }
        return text.substring(start, end);
    }

    private Optional<String> valueFromAnswerSegment(String segment) {
        // If the explicit answer is written as an expression, e.g. "2 + 3 = 5",
        // the right-hand side is the committed value.
        Optional<String> equals = lastMatch(EQUALS_SIGN, segment);
        if (equals.isPresent()) {
            return equals;
        }

        Matcher number = FALLBACK_NUMBER.matcher(segment);
        return number.find() ? Optional.of(number.group(1).trim()) : Optional.empty();
    }

    private Optional<String> latestSecondaryCandidate(String text) {
        List<Candidate> candidates = new ArrayList<>();
        collectMatches(EQUALS_SIGN, text, candidates);
        collectMatches(RESULT_PHRASE, text, candidates);

        return candidates.stream()
                .max(Comparator.comparingInt(Candidate::position))
                .map(Candidate::value);
    }

    private void collectMatches(Pattern pattern, String text, List<Candidate> candidates) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            candidates.add(new Candidate(matcher.start(), matcher.group(1).trim()));
        }
    }

    private Optional<String> lastMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        String last = null;
        while (matcher.find()) {
            last = matcher.group(1).trim();
        }
        return Optional.ofNullable(last);
    }

    private Optional<String> singleFallbackNumber(String text) {
        Matcher matcher = FALLBACK_NUMBER.matcher(text);
        String only = null;
        int count = 0;
        while (matcher.find()) {
            only = matcher.group(1).trim();
            count++;
            if (count > 1) {
                return Optional.empty();
            }
        }
        return count == 1 ? Optional.of(only) : Optional.empty();
    }

    private record Candidate(int position, String value) {
    }
}
