package Module;

import java.util.Random;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import Helper.ConsoleIO;

import Data.Type.Question;

import Data.Const;


public class SlangQuestion {

    private SlangDictionary dictionary;
    private final Random random = new Random();


    /**------------ Methods ------------*/

    /**
     * Constructs a new SlangQuestion instance with the specified slang dictionary.
     *
     * @param dictionary The slang dictionary to use for generating questions
     */
    public SlangQuestion(SlangDictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Builds a quiz asking for the meaning of a slang key.
     * <p>
     * The question is created by {@link #createSlangQuestion()}.
     * Then calls {@link #showQuiz()} to start the quiz.
     * </p>
     */
    public void buildSlangQuiz() {
        showQuiz(createSlangQuestion());
    }

    /**
     * Creates a question asking for the meaning of a slang key.
     * <p>
     * Selects one correct meaning and samples distinct distractors from other
     * entries. Excludes all meanings of the selected key, then shuffles the
     * choices. Sampling stops at four choices or after {@link Const#MAX_SAMPLING_TRIES} attempts.
     * </p>
     *
     * @return a shuffled question ready to display
     */
    public Question createSlangQuestion() {
        // Select a random slang key and one of its definitions as the correct answer.
        String slang = dictionary.getRandomSlang();
        List<String> slangDefinitions = dictionary.getDefinitions(slang);
        String answer = slangDefinitions.get(random.nextInt(slangDefinitions.size()));
        List<String> choices = new ArrayList<>();

        // Include the correct meaning before collecting distinct distractors.
        choices.add(answer);

        int tries = 0;

        // Sample other slang keys and their definitions until reaching four choices.
        while (choices.size() < 4 && tries++ < Const.MAX_SAMPLING_TRIES) {
            String anotherSlang = dictionary.getRandomSlang();

            if (anotherSlang.equalsIgnoreCase(slang)) {
                continue;
            }

            // Select a random definition of the other slang key as a distractor.
            List<String> anotherDefinitions = dictionary.getDefinitions(anotherSlang);
            String distractor = anotherDefinitions.get(random.nextInt(anotherDefinitions.size()));

            // Avoid offering another correct meaning as a distractor.
            if (!slangDefinitions.contains(distractor)) {
                choices.add(distractor);
            }
        }

        // Build the prompt and display the quiz with shuffled choices.
        String prompt = "What is the meaning of the slang key: \"" + slang + "\"?";
        return shuffle(prompt, choices, answer);
    }

    /**
     * Builds a quiz asking for the slang key of a definition.
     * <p>
     * The question is created by {@link #createDefinitionQuestion()}.
     * Then calls {@link #showQuiz()} to start the quiz.
     * </p>
     */
    public void buildDefinitionQuiz() {
        showQuiz(createDefinitionQuestion());
    }

    /**
     * Creates a question asking for the slang key of a definition.
     * <p>
     * Selects one definition and its matching slang key, then samples distinct
     * keys that do not share that exact definition. Shuffles the result after
     * reaching four choices or after {@link Const#MAX_SAMPLING_TRIES} attempts.
     * </p>
     *
     * @return a shuffled question ready to display
     */
    public Question createDefinitionQuestion() {
        // Select a random slang key and one of its definitions as the prompt.
        String slang = dictionary.getRandomSlang();
        List<String> definitions = dictionary.getDefinitions(slang);
        String answer = definitions.get(random.nextInt(definitions.size()));
        List<String> choices = new ArrayList<>();

        // Include the matching key before sampling other dictionary entries.
        choices.add(slang);

        int tries = 0;

        // Sample other slang keys until reaching four choices or the attempt limit.
        while (choices.size() < 4 && tries++ < Const.MAX_SAMPLING_TRIES) {
            String anotherSlang = dictionary.getRandomSlang();

            // Exclude every other key that also has the selected meaning.
            if (!dictionary.getDefinitions(anotherSlang).contains(answer)) {
                choices.add(anotherSlang);
            }
        }

        // Build the prompt and display the quiz with shuffled choices.
        String prompt = "Which slang key has the definition: \"" + answer + "\"?";
        return shuffle(prompt, choices, slang);
    }

    /**
     * Shuffles prepared choices and locates the correct answer.
     * <p>
     * Copies the distinct choice set into a list, shuffles it with the shared
     * random generator, and calculates the answer's index in that final order.
     * </p>
     *
     * @param prompt Slang key or definition used as the question prompt
     * @param choices Distinct choices including the correct answer
     * @param answer Correct answer text
     * @return A question containing the shuffled choices and correct index
     */
    private Question shuffle(String prompt, List<String> choices, String answer) {
        Collections.shuffle(choices, random);
        return new Question(prompt, choices, choices.indexOf(answer));
    }

    /**
     * Displays a quiz question and checks a valid answer selection.
     * <p>
     * Labels the available choices with letters starting at A and retries until
     * the user selects an available letter. Reports success or reveals the
     * correct letter and choice text after an incorrect answer.
     * </p>
     *
     * @param prompt Complete question text to display
     * @param question Question data containing choices and the correct zero-based index
     */
    private void showQuiz(Question question) {
        System.out.println("\n" + question.prompt);

        char[] labels = {'A', 'B', 'C', 'D'};

        // Display the available choices with letter labels.
        for (int i = 0; i < question.choices.size(); i++) {
            System.out.println(labels[i] + ". " + question.choices.get(i));
        }

        int selectionIndex = -1;

        // Keep prompting until a valid selection is made.
        while (selectionIndex < 0 || selectionIndex >= question.choices.size()) {
            String selection = ConsoleIO.input("Please select your answer: ")
                .trim().toUpperCase();
            selectionIndex = selection.length() == 1 ? selection.charAt(0) - 'A' : -1;
        }

        // Report the selected answer after a valid choice has been entered.
        if (selectionIndex == question.correctIndex) {
            System.out.println("\nYour answer is correct!");

        } else {
            System.out.println("\nYour answer is incorrect. The correct answer is: " +
                labels[question.correctIndex]
                + ". " + question.choices.get(question.correctIndex));
        }
    }
}
