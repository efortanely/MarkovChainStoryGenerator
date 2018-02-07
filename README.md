# MarkovChainStoryGenerator
Overview.

The Markov Text Generator can be used to create nonsensical passages composed of phrase patterns captured from the original text input. Markov chains describe the probability of a new event’s occurrence, given some previous state. With this, the generator is able to probabilistically generate new phrases based on a prediction of what a new phrase will be given some number of phrases that precede it. However, as it is unable to combine long term and short-term patterns in a unique configuration, its generated stories are rather confusing. A better solution for this problem would be to implement something like a LSTM network (Long short-term memory), which is a type of recurrent neural network capable of handling both of these nuances with more convincing results of recreating events that occur in sequence, such as music or text generation, with less need to write code to accomodate special cases and more convincing recreation of long term patterns.

////

Example of a simple story generation.

StoryGenerator story = new StoryGenerator();

story.makeStory();

story.printStory();

////

Creating a story.

The constructors provided allow for the user to provide input from a file or from the console. The default constructor assumes console input. Additonal constructors provided allow for inputting a ‘Story’ from the Story enum of prestored novel samples, or inputting the path for a .txt file created by the user containing a text sample.

Configurations.

There are a number of settings to be tweaked for fun new results! The ‘chain length’ refers to the length of the phrase to be used as the basis for what new phrase will be created. For example, if my phrase had chain length 2 and the sample text contained “the dog eats food” and “the dog is cute”, then a possible phrase would be “the dog” with a 50% chance of “dog eats” following the phrase and likewise for “dog is”. The minimum number of words in the output is set with ‘numOutputWords’. The minimum length of each output line is controlled by ‘outputWidth’. Finally, if you liked the output story, the seed, whose default is set to the system time, can be retrieved for later use. Additionally, the seed can always be set to a seed specified by the user before story generation.

Processing and creating output.

There are two main stages to story generation: the process of scanning through the input file and storing phrases and the associated phrases following them, and the generation of a new story by probabilistically walking through these phrases.

A BufferedReader is used to parse through the data because it offers a more optimal parsing time than a Scanner. The boolean flags ‘containsCapitals’ and ‘containsPunctuation’ are used for creating output that better approximates patterns in the input text, and are set to true if found in input text. A HashMap of Strings associated with an ArrayList of Strings is used to link each phrase to the phrases that follow it. An initial phrase, of up to 'chainLength' size, is grabbed from the input, and appended to a StringBuffer, which will act as a buffer to store the new key phrase. For the rest of the input, the current value from the string buffer is searched for in the HashMap, retrieving the ArrayList or creating a new one if the key has not been entered in the HashMap yet. The first word is deleted from the current phrase. The next word is retrieved from the input file and added to the string buffer. This following phrase is then added to the ArrayList of Strings associated with the key, the phrase that preceded this updated phrase. In the HashMap, phrases maintain their unique capitalization and punctuation in an attempt to maintain style. Termination conditions for the end of the read-in phase depends on the type of Reader implemented, either a FileReader or InputStreamReader, in case of user input text. Input parsing ends for user input if the current token equals “\\end”. Input parsing ends for file input if no remaining strings are left in the BufferedReader.

The keySet from the HashMap of all possible phrases found in the text is stored in an ArrayList of Strings, phrases, for faster random access throughout the generation process. An initial seed is selected randomly from phrases, and if capitals are present in the input file, then a new seed will be selected until it begins with a capital letter. Then, one of two phases occur: a valid phrase(s) follows the current seed, or there are no given phrases following the current seed. If no phrases follow the current phrase, then a new seed is randomly selected from phrases. If the input text contains capitalization and punctuation, then if the last output word to the story ends in a period, a new seed will be selected until it begins with a capital letter to maintain style~. In the case that phrases do follow the seed, a new seed will be selected from the ArrayList associated with the current phrase, in the HashMap, which acts as a way to select them probabilistically. This method of storing phrases multiple times, rather than storing a probability with each phrase, is easier to read and has less overhead storage in cases of series of multiple words, where for many instances, a small number of duplicate phrases follow any given phrase. Finally, the new output is appended to the output StringBuffer, which is an entire phrase if a new seed was selected, or a single word if the last seed had phrases following it in the HashMap, since the preceding words will have already been output. The ‘textCounter’, which maintains the number of characters in the current line, is updated, and if the number of characters exceeds ‘outputWidth’, then a new line is added to the output. If the total number of output words exceeds or is equal to ‘numOutputWords’, then if no punctuation or capitalization is present in the input file, the output process stops. If stylization is present, then the output process continues until the last word in the output is a period. This buffer is then saved as a String to ‘generatedString’.
