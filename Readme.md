Simple List
=
Show a simple list of users' information from multiple remote data source.

Based on software layered architecture pattern, separate three modules to focus on each respective businesses.

The Business Layer uses RxJava and Paging to make a async loading source.

By the way, this project imports a shell module to obfuscate dex file for testing, but it also makes apk size larger.