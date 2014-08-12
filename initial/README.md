A small test application that uses the Google Play Developer API to publish APKs to linked projects.

You need to build it:

```
./gradlew installApp
```

You need to create a Google API Console project, create an oauth2 'Other' application and download 
the JSON file for it.

You need to link the Google API Console project to a Google Play publisher account.

You need to run the program:

```
./initial/build/install/initial/bin/initial packagename path_to_apk
```

It'll fire up a browser to ask for credentials, so log in with your publisher account to give the 
app a valid oauth credential. Cut and paste the auth code when asked.