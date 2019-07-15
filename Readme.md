# Amazon Search Term Volume Estimation   
## Building the project  
  
The project can be built by running:
  
 ``` mvn clean install dockerfile:build ```
   
This produces a .jar file and a docker image.  
  
## Running the project  
  If building of the project completed successfully, the project can be run either from the .jar directly:  
 
 ```java -jar target/atrtask-1.0-SNAPSHOT.jar```  
 
  or via docker:
   
``` docker run -it -p 8080:8080 ```  
  
### Helpfull settings:
  
```properties  
  
 server.port=8080 
 com.sellics.atrposki.restapi.runningtime_in_nanoseconds=9000000000 
 com.sellics.atrposki.restapi.default_mkt=1 
 com.sellics.atrposki.restapi.default_department=aps
   
```   
  
## General idea  
(Note: I will use search term, word and keyword interchangeably, and they all mean the same in this document. This stretches the meaning of "word" a bit more towards a sentence)  
  
Task: We need to estimate how "hot" a exact search term is.   
  
Resources: Amazon gives us an api that is going to return in a best case scenario the 10 hottest words matching our search term as a prefix.  
  
The main idea here is in the words __not__ returned by amazon. Why are they "pruned"? Well, its either because they weren't a match for the prefix, or they weren't hot enough. We are interested in differentiating between those two.
Now, I would love to master the dark arts of bayesian probability, but since that is not the case this is what I came up with. 

  
If a word is really hot, it is going to show up as soon as I type its first letter.  
The more letters I have typed, the more likely it is that the results we are seeing are not hot, but just specific.   
So a good way to estimate how hot a word is is to run this process backwards. Assume a word has 0 hotness, and start searching for its sub prefixes;  
For each subprefix of the word, the smaller that subprefix the more different words would not get "pruned" based on not matching.  
The more subprefixes we run our query with, and yet still see the original keyword in the results, the more we can be sure that our word is indeed hot and not just specific.   
Example: the query "apple watch"  
``` prefix      -> is the original query in the results?  
apple watch -> "apple watch" is in the result  
apple watc  -> stil there  
apple wat   -> stil there  
apple wa    -> stil there  
apple w     -> stil there  
apple       -> stil there  
appl        -> stil there  
app         -> stil there  
ap          -> stil there  
a           -> stil there  
```  
As it turns out apple watch is a really popular term and scores max with this approach.  
Lets check: "iphone charger"  
```bash  
 iphone cahrger -> hit  
 iphone cahrge  -> hit  
 iphone cahrg   -> hit  
 iphone cahr    -> hit   
 iphone cah     -> hit  
 iphone ca      -> hit  
 iphone c       -> hit  
 iphone         -> hit  
 iphon          -> hit  
 ipho           -> miss  
    
```  
With the iphone charger it turns out, it is not as popular as other terms starting with "ipho".  
If we imagine all amazon data as a huge Trie, the deeper we go traversing it the more we prune by the prefix, the less important the hotness is.  
However, all prefixes are not created equal. Some prefixes are naturally very popular so winning the "a" subtrie would be a much bigger win then wining the "x" subtrie.   
In the approach of this algorithm, we turn a blind eye to this.   
More over, we turn a blind eye for the lack of mechanisms to compare what is more popular between words with a different first letter.  
  
  
## b) The algorithm  
As discussed above, a search term hotness would be measured by how many of that search terms prefixes still result in the search term as an autocomplete suggestion.  
A couple minor details:  
* We need to normalize by word length. The score should be number of occurrences in search results over total number of subprefixes, so different word lengths would fit on the same fuzzy scale.  
* Not all search results are equal. If there are less then 10 results from a query its a good sign that we are into a very specific space of suggestions instead of a hot space of suggestions. We can modify the "counting" of the point above and add "weight" to each count.Simply its used NumberOfResults/MaximumNumberOfResults  
* Not all word endings are important. Trailing spaces are almost always meaningless. If we goo deeper, we can find a lot more language specific "stop words", but for now, we need only consider them as a configurable list. Just spaces will suffice for now.  
* We need a iterative approach. At any given time we can interrupt the process.  
  
The algorithm in pseudocode:  
```bash  
score=0;  
Foreach subprefix of originalQuery:  
 results = apicall(subprefix); 
 if(results.contains(originalQuery))
    score += calculateIterationWeigth(results)  
 else  
    break;  
 
  if(timeOut())  
    break;    
    
return score/originalQuery.length  
```  
  __calculateWeight__ in the code above,  simply divides the number of results with the maximum possible number of results per query.  
This is overly simplistic. Even without any linguistic knowledge, we can assume that the smaller the prefix the bigger the weight of the iteration. (this is not implemented)  
  
Contrasting that, __score/originalQuery.length__ is overly simplified in the pseudocode because in the implementation, it disregards stop words.  
  
  
   
  
  
  
  
## a) Assumptions  
 * A score of 100 means that even if just the first letter of a word is typed, the autocomplete suggest the search term. This would make the "hot" list of words with score 100, based on the language/marketplace somewhere between 260-500 words at any given time according to the algorithm implemented here.  
 * Words have uniform frequency across prefixes. There are equal number of words starting with X, as they are starting with S. (this is obviously false)  
 * Amazon does not personalize the requests. Searching multiple times for similar terms from the same machine will not bias future search results  
 * There are stop words/stop suffixes, and they are absolute and independent of positioning, context or marketplace. This means that certain strings can be added to the search term with no impact on the search results, and visa versa, they can also be removed. An example of such string would be the white space. Stop words can be ignored for all intents and purposes. (this is not true in the general case and may vary wildly based on language)  
 * Capitalization does not matter.  
 * All results return by amazon api have an exact prefix match with the search term  
 * The majority of the round trip time is going to be spend on waiting for the amazon api. Serialization/Desrialization cost and the actual algorithm are very short  
 * The cap number of results amazon returns is unlikely to change  
 * Stop words are static, independent of marketplace, and unlikely to change.  
 * Amazon will always return an empty array instead of a null result  
  
## d) How precise is the outcome  
  The outcome should be really precise for the extremes.   
   Short and really cold words, should be precisely detected as well as long and hot words.  
    
   The algorithm is not precise across prefixes. There is not a good way to compare words with different first letters with this approach and the "hotness" might be heavily biased to certain letters.  
    This can be improved by analysing word databases and average word length. We can play a lot more with the weight of each iteration if we know how likely a word is to be that long. Or even better how likely is one word to be a prefix.  
      
   We should be able to put some upper and lower bounds on the precision based on this improvements.  
  
## c) Is it true that: All results returned in by the same query are in the roughly same ballpark of hotness?  
  It seems like yes, but it is really difficult to gauge.   
   The more precise a search query is the less deviation I would expect to see in the hotness score of the results.  
      
   I would also expect to see less deviation in hotness the smaller the number of results of an api call.  
     
   In the top scorers and small prefix world, order might be more important purely based on the magnitude of the numbers.  
   Comparatively small difference between really hot terms might be a significant on an absolute scale in other more specific queries.     
     
   Unfortunately, I can see counterexamples for everything mentioned above.  
     
     
   So even though it feels right and true, I am not sure I can fully prove it. I think the key word in the hint is "comparatively" and it really feels like yes, but I would like some solid math behind it and not just plausable educated guesses. (I don't have it :( )
     
  
# Ideas graveyard  
* Add weights to prefixes based on how common they are in the English language  
  * Requires preprocessing /databases/caching which was forbiden  
  * I doubt "CR9EH-9" is a common prefix in the English language, but I spent hours searching for it on amazon. Speach/everyday language patterns do not always correspond with search patterns  
* Use levenshtein/hamming distance to estimate how many words we prune with each prefix. Runing a quick bread first search for the nearest results/prefixes should give us an estimate of how much we are prunning based on specificity.  
  * Computationaly expensive easey to burn the 10 seconds limit  
  * Not really bringing too much on the table  
* Measure response time to check if a search result is cached. I would expect a "hot" word to be easier to retrieve purely based on it actually being in the cache.  
  * Difficult to measure, and prone to noise  
  * Discrete signal. Either things are cached, or not.  
* Measure response time to check for operation asymmetry. I would expect filtering based on prefix to be of different length then filtering based on popularity. Moreover finding something is not popular should take longer.  
  * Difficult to measure and prone to noise  
  * Doesn't need to be true, there can be denormalized data to make all reads and sorts equally quick  
  * Even the same data/database may decide on different execution plan based on current parameters
