Command line driven text search engine, with usage: 

```
> sbt
> runMain SearchMain directoryContainingTextFiles 
```

This reads all the text files in the given directory, gives a command prompt at which interactive searches can be performed. 

An session looks like: 

```
> sbt
> runMain SearchMain /foo/bar
14 files read in directory /foo/bar

search> to be or not to be 
file1.txt: 100% 
file2.txt: 90% 

search> cats 
no matches found 

search> :quit 
```

The search takes the words given on the prompt and returns a list of the top 10 (maximum) matching filenames in rank order, giving the rank score for each match respectively. 