import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

//A naive nonsensical text generator intended for use with English writing, implemented with Markov chains.
//Inspired by my friends, Emma and Amy, and their passion for writing fiction and reading classic novels :)
public class TextGenerator {

	// story names for ease of access in story generator constructor
	private enum Story {
		DavidCopperfield(0), TheCountOfMonteCristo(1), ThePictureOfDorianGray(2), ATaleOfTwoCities(3), Metamorphosis(
				4), HeartOfDarkness(5), AliceInWonderland(6), GrimmsFairyTales(7);
		private int index;

		private Story(int index) {
			this.index = index;
		}

		protected int getIndex() {
			return this.index;
		}
	};

	private static class StoryGenerator {
		// Maps each phrase of length chainLength to a list of all possible phrases it
		// leads to
		private HashMap<String, ArrayList<String>> frequencyMappings;
		// markov chain length
		private int chainLength;
		// boolean flags used for capturing next appropriate phrase
		private boolean containsCapitals;
		private boolean containsPunctuation;
		// minimum number of output words, output continues to end of sentence if
		// punctuation exists in sample text
		private int numOutputWords;
		// minimum width of output text
		private int outputWidth;
		// used for seeding the random object
		private long randomSeed;
		private Random rand;
		// allows input from a file or console
		private Reader inputStoryReader;
		// final output created by markov chain
		private String generatedText;

		// generate from text, accepting file names for story
		public StoryGenerator(String fileName) throws IOException {
			this.setDefaultValues();
			try {
				this.inputStoryReader = new FileReader(fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// generate from text, accepting story name associated with file
		public StoryGenerator(Story storyName) {
			this.setDefaultValues();
			String[] files = { "david.txt", "monte.txt", "dorian.txt", "cities.txt", "metamorphosis.txt",
					"darkness.txt", "alice.txt", "fairy.txt" };
			try {
				this.inputStoryReader = new FileReader(files[storyName.getIndex()]);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// generate from text, from human input (or robot input? do you have a robot
		// that can type? can I borrow it? asking for a friend)
		public StoryGenerator() throws IOException {
			this.setDefaultValues();
			this.inputStoryReader = new InputStreamReader(System.in);
			System.out.println("Enter your sample text in the console, followed by a space and \"\\end\"!");
		}

		private void setDefaultValues() {
			this.frequencyMappings = new HashMap<>();
			// a small chain length is nonsensical, a large chain length will tend towards
			// directly copying passages from the text unless there are a lot of repeated
			// passages of chainLength
			this.chainLength = 2;
			this.containsCapitals = false;
			this.containsPunctuation = false;
			this.numOutputWords = 500;
			this.outputWidth = 70;
			this.randomSeed = System.currentTimeMillis();
			this.rand = new Random(this.randomSeed);
		}

		public void makeStory() throws IOException {
			this.parseInput();
			this.generateOutput();
		}

		public long getSeed() {
			return this.randomSeed;
		}

		public void setSeed(long seed) {
			this.randomSeed = seed;
			this.rand = new Random(this.randomSeed);
		}

		public void setMarkovChainLength(int length) {
			this.chainLength = length;
		}

		// the minimum number of words that will be output, the program continues
		// until the current sentence ends
		public void setOutputLength(int length) {
			this.numOutputWords = length;
		}

		// the minimum character width for each line
		public void setOutputWidth(int width) {
			this.outputWidth = width;
		}

		public void printStory() {
			System.out.println(this.getStory());
		}

		private String getStory() {
			return this.generatedText;
		}

		private void parseInput() throws IOException {
			BufferedReader br = new BufferedReader(this.inputStoryReader);
			StringTokenizer st;
			// the string buffer allows for continuity over new lines, storing the history
			// of the last #(chainLength) words
			StringBuffer sb = new StringBuffer();
			boolean endOfInput = false;
			boolean waitCondition = this.inputStoryReader instanceof FileReader ? br.ready() : !endOfInput;

			while (waitCondition) {
				st = new StringTokenizer(br.readLine());
				while (st.hasMoreTokens()) {
					// if the buffer has not been initialized, add the first #(chainLength) words
					if (sb.length() == 0) {
						for (int i = 0; i < this.chainLength && st.hasMoreTokens(); i++) {
							String next = sb.length() == 0 ? st.nextToken() : " " + st.nextToken();
							sb.append(next);
						}
					}
					// otherwise, the input is parsed in phrase sizes of #(chainLength) words

					// the current phrase in the string buffer is considered the key

					// to get the phrase following the key phrase, remove the first word and add the
					// next word from the string tokenizer to the end

					// the value associated with the current phrase contains all possible phrases
					// following the key, so this new phrase is added to the corresponding key's
					// arraylist

					// this functions as a way to deterministically select a following phrase, with
					// its probability of following the current phrase corresponding to its
					// frequency of occurrence in the list
					else {
						String currentPhrase = sb.toString();
						// remove italic flags
						currentPhrase.replaceAll("_", "");

						// capture if the text contains capitals and or punctuation to be used for
						// creating later text generation
						if (!this.containsCapitals) {
							if (currentPhrase.matches("[A-Z]+.+"))
								this.containsCapitals = true;
						}

						if (!this.containsPunctuation) {
							if (currentPhrase.endsWith("."))
								this.containsPunctuation = true;
						}

						ArrayList<String> nextPhrases = this.frequencyMappings.get(currentPhrase);
						if (nextPhrases == null)
							nextPhrases = new ArrayList<>();

						// delete the first word from the buffer
						sb.delete(0, sb.indexOf(" ") + 1);
						String next = st.nextToken();

						// ugly boolean logic because FileReader inherits from InputStreamReader, so
						// this is checking if it's input from the user and if the token marks the end
						// of input
						if (!(this.inputStoryReader instanceof FileReader) && next.equals("\\end")) {
							endOfInput = true;
						} else {
							// add the next word to the buffer
							sb.append(" " + next);
							nextPhrases.add(sb.toString());
							this.frequencyMappings.put(currentPhrase, nextPhrases);
						}

						waitCondition = this.inputStoryReader instanceof FileReader ? br.ready() : !endOfInput;
					}
				}
			}

			br.close();
		}

		private void generateOutput() {
			// used for marking if output should continue even after the total number of
			// words has been satisfied in the case where the sentence should be completed
			boolean continueOutput = true;

			ArrayList<String> phrases = new ArrayList<>(this.frequencyMappings.keySet());
			String seed = phrases.get(this.rand.nextInt(phrases.size()));
			// select a seed beginning with a capital letter
			if(this.containsCapitals) {
				while (seed.charAt(0) < 'A' || seed.charAt(0) > 'Z')
					seed = phrases.get(this.rand.nextInt(phrases.size()));
			}

			// generated story!!! whoo!!! this is what we're here for. in case you weren't
			// sure
			StringBuffer output = new StringBuffer(seed + " ");
			// current number of characters output on current line, used to check when a
			// newline should be appended to output
			int textCounter = seed.length();

			for (int i = this.chainLength; i < this.numOutputWords || continueOutput; i++) {
				// retrieve all phrases following the current phrase ("seed")
				ArrayList<String> nextPhrases = this.frequencyMappings.get(seed);
				String newOutput = "";

				// if no phrases follow the current seed, randomly select a new seed, pertaining
				// to input style (capitals, punctuation)
				if (nextPhrases == null) {
					String newSeed = phrases.get(this.rand.nextInt(phrases.size()));

					if(this.containsCapitals && this.containsPunctuation) {
						// capitals and punctuation
						String finalWord = seed.substring(seed.lastIndexOf(' ') + 1, seed.length());
						// seed contains a period at the end
						if (finalWord.contains(".")) {
							// get new seed starting with a capital
							while (newSeed.charAt(0) < 'A' || newSeed.charAt(0) > 'Z')
								newSeed = phrases.get(this.rand.nextInt(phrases.size()));
						} else {
							// get new seed that doesn't start with a capital
							while (newSeed.charAt(0) >= 'A' && newSeed.charAt(0) <= 'Z')
								newSeed = phrases.get(this.rand.nextInt(phrases.size()));
						}
					}

					// set the seed for the next phrase generation equal to the randomly selected
					// phrase from the hash map
					seed = newSeed;
					// set the new output to the entire selected phrase, the other method only adds
					// the last word to the new output!
					newOutput = seed;
				} else {
					// set the seed equal to the probabilistically selected phrase following the
					// current seed
					seed = nextPhrases.get(this.rand.nextInt(nextPhrases.size()));
					String finalWord = seed.substring(seed.lastIndexOf(' ') + 1, seed.length());
					// otherwise, if the seed is a continuation of the last printed phrase, simply
					// set the new output equal to the only new word, the last word in the phrase
					newOutput = finalWord;
				}

				// create new lines when text width has exceeded output width
				output.append(newOutput + " ");
				textCounter += newOutput.length() + 1;
				if (textCounter >= this.outputWidth) {
					output.append("\n");
					textCounter = 0;
				}

				// continue to the end of the last sentence after the desired number of words
				// has been output, or if sentences don't exist in the input paradigm, just end
				// the for loop
				if (i >= this.numOutputWords) {
					if (this.containsPunctuation) {
						String lastOutputWord = newOutput.substring(newOutput.lastIndexOf(' ') + 1, newOutput.length());
						if (lastOutputWord.endsWith("."))
							continueOutput = false;
					} else
						continueOutput = false;
				}
			}
			// save the buffer
			this.generatedText = output.toString();
		}

	}

	public static void main(String[] s) throws IOException {
		Story[] allStories = Story.values();
		for (int i = 0; i < Story.values().length; i++) {
			StoryGenerator st = new StoryGenerator(allStories[i]);
			System.out.println("Seed for given passage: " + st.getSeed());
			st.setOutputLength(100);
			st.makeStory();
			st.printStory();
			System.out.println();
		}
	}
}