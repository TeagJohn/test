package com.dse.util;

/*
MIT License

Copyright (c) 2017 Reason

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A factory class used to generate Matcher instances that will
 * throw if in use after their timeout.
 */
public abstract class TimeLimitedMatcherFactory {
    // If a regular expression requires more than a couple of seconds
    // to complete, then it has no place in polite society.
    private static final long RE_TIMEOUT = 2000L;

    /**
     * Generate a Matcher instance that will throw if used or still
     * in use more than timeoutInMilliseconds after its instantiation.
     * <p>
     * Use the instance immediately and then discard it.
     *
     * @param pattern               The Pattern instance.
     * @param charSequence          The CharSequence to operate on.
     * @param timeoutInMilliseconds Throw after this timeout is reached.
     */
    public static Matcher matcher(
            Pattern pattern,
            CharSequence charSequence,
            long timeoutInMilliseconds
    ) {
        // Substitute in our exploding CharSequence implementation.
        if (!(charSequence instanceof TimeLimitedCharSequence)) {
            charSequence = new TimeLimitedCharSequence(
                    charSequence,
                    timeoutInMilliseconds,
                    pattern,
                    charSequence
            );
        }

        return pattern.matcher(charSequence);
    }

    /**
     * Generate a Matcher instance that will throw if used or still
     * in use more than 2 seconds after its instantiation.
     * <p>
     * Use the instance immediately and then discard it.
     *
     * @param pattern      The Pattern instance.
     * @param charSequence The CharSequence to operate on.
     */
    public static Matcher matcher(
            Pattern pattern,
            CharSequence charSequence
    ) {
        return matcher(pattern, charSequence, RE_TIMEOUT);
    }

    /**
     * An exception to indicate that a regular expression operation
     * timed out.
     */
    public static class RegExpTimeoutException extends RuntimeException {
        public RegExpTimeoutException(String message) {
            super(message);
        }
    }

    /**
     * A CharSequence implementation that throws when charAt() is called
     * after a given timeout.
     * <p>
     * Since charAt() is invoked frequently in regular expression operations
     * on a string, this gives a way to abort long-running regular expression
     * operations.
     */
    private static class TimeLimitedCharSequence implements CharSequence {
        private final CharSequence inner;
        private final long timeoutInMilliseconds;
        private final long timeoutAfterTimestamp;
        private final Pattern pattern;
        private final CharSequence originalCharSequence;

        /**
         * Default constructor.
         *
         * @param inner                 The CharSequence to wrap. This may be a subsequence of the original.
         * @param timeoutInMilliseconds How long before calls to charAt() throw.
         * @param pattern               The Pattern instance; only used for logging purposes.
         * @param originalCharSequence  originalCharSequence The original sequence, used for logging purposes.
         */
        public TimeLimitedCharSequence(
                CharSequence inner,
                long timeoutInMilliseconds,
                Pattern pattern,
                CharSequence originalCharSequence
        ) {
            super();
            this.inner = inner;
            this.timeoutInMilliseconds = timeoutInMilliseconds;
            // Carry out this calculation here, once, rather than every time
            // charAt() is invoked. Little optimizations make the world turn.
            timeoutAfterTimestamp = System.currentTimeMillis() + timeoutInMilliseconds;
            this.pattern = pattern;
            this.originalCharSequence = originalCharSequence;
        }

        public char charAt(int index) {
            // This is an unavoidable slowdown, but what can you do?
//            if (System.currentTimeMillis() > timeoutTime) {
            if (System.currentTimeMillis() > timeoutAfterTimestamp) {
                // Note that we add the original char sequence to the exception
                // message. This condition can be met on a subsequence of the
                // original sequence, and the subsequence string is rarely
                // anywhere near as helpful.
                throw new RuntimeException(
                        "Regular expression timeout after " + timeoutInMilliseconds
                                + "ms for [ " + pattern.pattern() + " ] operating on [ "
                                + originalCharSequence + " ]"
                );
            }
            return inner.charAt(index);
        }

        public int length() {
            return inner.length();
        }

        public CharSequence subSequence(int start, int end) {
            // Ensure that any subsequence generated during a regular expression
            // operation is still going to explode on time.
            return new TimeLimitedCharSequence(
                    inner.subSequence(start, end),
                    timeoutAfterTimestamp - System.currentTimeMillis(),
                    pattern,
                    originalCharSequence
            );
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }
}
