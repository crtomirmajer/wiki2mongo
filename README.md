# wiki2mongo

wiki2mongo is small java library for importing Wikipedia articles from xml dump into MongoDB. It also removes wiki markup, infoboxes, html tags, reference tables etc. before storing.

It uses Akka Streams for concurrency.

# Performance

wiki2mongo is able to process and store 12 million articles in less than 2 hours on 6 core machine with SSD.
