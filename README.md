# Sugoi Search Engine

## What is this?
Trivially from the name, a search engine. This Java based collection of programs crawls the internet, downloads HTML pages, and indexes them using MongoDB.

There is also a web interface used to query for results.

## What is currently implemented 
1. Crawling   
2. Stemming
3. Indexing
4. Web Interface

## Crawling
If there are no available links in the URL collection, the crawler first loads "seed.txt" and begins crawling from there.

If there are any available links in the URL collection, each thread (crawler) removes a URL and visits it.

crawlers check robots.txt to figure out whether they are authorized to download the page or not. If they are, they download the page's HTML and extract links from it, then insert both in their respective collections.

The crawling continues until crawlers run out of URLs or reach their specified page threshold.

## Stemming
To provide better search results and lower database overhead, similar words such as: "computer", "computing", "compute", etc... are reduced to their base word "comput". The stemmer does this for various types and forms of words.

## Indexing
This step includes inserting the stemmed, the link it was found in, its position in that page, and where it was (whether it was an h1, title, or something else).

We also store things the other way around, with each page having its words, word positions, and word tags also being stored.

## Web interface
The web interface allows users to use text or speech queries. The interface returns text results along with snippets from each page.

## To do
- Fix duplicate page crawls from different URLs

    This can be done by storing a compressed version of already visited pages (By taking the first character of each word for example), doing the same to the just downloaded URL, then checking how much they match.
- Improving the indexer's speed
- Fixing result pagination
- Introducing result ranking
- Probably some more stuff that I can't currently remeber