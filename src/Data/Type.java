package Data;

import java.util.List;

/**
 * Defines data types used in the application.
 */
public class Type {

    /**
     * Holds the prompt, answer choices, and correct index of a quiz question.
     * <p>
     * The prompt is either a slang key or a definition, depending on the quiz
     * direction. The console uses the ordered choices and zero-based correct
     * index to label and check the user's selected answer.
     * </p>
     */
    public static class Question {

        public final String prompt;
        public final List<String> choices;
        public final int correctIndex;


        /**------------ Methods ------------*/

        /**
         * Creates a question from prepared answer choices.
         * <p>
         * Stores the supplied prompt, choice list, and correct index without
         * reordering them. The dictionary prepares and shuffles choices first.
         * </p>
         *
         * @param prompt Slang key or definition shown as the prompt
         * @param choices Answer choices in their display order
         * @param correctIndex Zero-based index of the correct answer
         */
        public Question(String prompt, List<String> choices, int correctIndex) {
            this.prompt = prompt;
            this.choices = choices;
            this.correctIndex = correctIndex;
        }
    }
}
