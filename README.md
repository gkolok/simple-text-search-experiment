Command line driven text search engine, with usage: 

```
> sbt
> runMain gk.searchengine.Main directoryContainingTextFiles 
```

This reads all the text files in the given directory, gives a command prompt at which interactive searches can be performed. 

An session looks like: 

```
> sbt
> runMain gk.searchengine.Main testdata
Failed to index file: pdf.txt, exception: java.nio.charset.MalformedInputException: Input length = 1
Failed to index file: pdf2.txt, exception: java.nio.charset.MalformedInputException: Input length = 1
4 files read in directory: C:\Users\Gabor_Kolok1\work\simple-text-search-experiment\testdata
search> to be or not to be
intro.txt: 100%
howtobbs.txt: 100%
ethics.txt: 100%
adventur.txt: 75%
search> cats
no matches found
search> :quit
```

The search takes the words given on the prompt and returns a list of the top 10 (maximum) matching filenames in rank order, giving the rank score for each match respectively. 