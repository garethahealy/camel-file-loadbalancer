camel-file-loadbalancer
===================
Sample code to have multiple camel file uri routes (either in same context or different), watching the same folder, i.e.: 'load balancer' for files

Current implementation works by the following logic:

1. Each file endpoint has a reference to a PriorityFileFilterFactory
2. The PriorityFileFilterFactory is assigned X number of endpoints which are watching the folder
3. The PriorityFileFilterFactory creates X PriorityFileFilters
4. Each PriorityFileFilter is given a priority starting from 0
5. Each endpoint 'Filter', is set to a PriorityFileFilter
6. Each endpoint 'MaxMessagesPerPoll' is set to the number of PriorityFileFilters created
7. Each endpoint 'Move' is set to .camelX - X being the priority of the PriorityFileFilter

This means that you end up with the following:

- 3 endpoints watching 1 folder
- Endpoint 0 has a priority of 0, thus gets the first file
- Endpoint 1 has a priority of 1, thus gets the second file
- Endpoint 2 has a priority of 2, thus gets the third file
- and so on...

If you are going to deploy the same endpoint, multiple times within the same context (as per lb-example), you will also need to set a value for 'uniqueKey'.
This is because Camel checks for endpoint uniqueness, we get around this with a slight hack

future
===================
Am also going to look into creating a 'fabric' (i.e. zookeeper backed) endpoint, which automatically scales.
Thus you don't have to set how many endpoints are watching the folder, it knows because of the number which has registered
