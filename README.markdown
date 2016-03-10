## Interview project

This implementation uses an in-memory Map to store the counts and all
pertinent information regarding the media files is received on the endpoint
/event. So votes will not be preserved across reboots.

The dropbox.token is found in: `src/main/resources/application.conf` and
should be configured to your Dropbox token. It will work best if you
configure your Dropbox application to have its own "App" folder (as the 
files are dropped in the root).

