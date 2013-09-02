Hospitality
===========

A simple hospitality scheduler for a small church

Todo
----

This is a very simple Java project that I've put here to work on after work hours so that I can have something to show people that isn't owned by my employer.

* **Concurrency**  Currently the program is build just to be run by a single use.  It would probably not be used by very many people at the same time but it still needs to be multi-user.
* **Persistence**  Currently the program uses a test file for persistence.  This was so that the file could be edited in, say, Excel, and imported into the back into the program.  With concurrency that will not work.  Some database will be used.
* **Data Access Objects**  The project should use DAOs to access the database, whatever it is.  It might be nice to allow the user to change from a DB to text.
* **Better UI**  Currently the program runs as two different windows that need to be launched separately.  It would be better to have one launch and be able to switch between forms.
