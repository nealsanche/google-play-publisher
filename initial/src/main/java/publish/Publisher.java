package publish;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.extensions.java6.auth.oauth2.GooglePromptReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.ApksListResponse;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;

public class Publisher {

    private static String PACKAGE_NAME;
    private static String APK_PATH;
    private static final String TRACK_NAME = "alpha";
    private static JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static NetHttpTransport httpTransport;
    private static FileDataStoreFactory dataStoreFactory;

    public static void main(String[] args) {

        for (String arg : args) {
            System.out.println("Argument: " + arg);
        }

        if (args.length < 1) throw new IllegalArgumentException("Argument 1 must be the package name of the app");
        if (args.length < 2) throw new IllegalArgumentException("Argument 2 must be the path to the apk");

        PACKAGE_NAME = args[0];
        APK_PATH = args[1];

        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory factory = JacksonFactory.getDefaultInstance();
            dataStoreFactory = new FileDataStoreFactory(new File("./"));

            Credential credential = authorize();
            credential.refreshToken();
            AndroidPublisher.Builder builder = new AndroidPublisher.Builder(httpTransport, factory, credential);
            builder.setApplicationName("Publisher");
            AndroidPublisher pub = builder.build();

            AppEdit aaa2 = pub.edits().insert(PACKAGE_NAME, null).execute();
            System.out.println(aaa2);

            String editId = aaa2.getId();
            System.out.println(editId);

            ApksListResponse execute1 = pub.edits().apks().list(PACKAGE_NAME, editId).execute();
            System.out.println(execute1.toPrettyString());

            Apk execute2 = pub.edits().apks().upload(PACKAGE_NAME, editId, new FileContent("application/vnd.android.package-archive", new File(APK_PATH))).execute();
            System.out.println(execute2);

            Track content = new Track();
            content.setTrack(TRACK_NAME);
            ArrayList<Integer> versionCodes = new ArrayList<>();
            versionCodes.add(execute2.getVersionCode());
            content.setVersionCodes(versionCodes);
            Track execute3 = pub.edits().tracks().update(PACKAGE_NAME, editId, TRACK_NAME, content).execute();
            System.out.println(execute3);

            AppEdit execute4 = pub.edits().commit(PACKAGE_NAME, editId).execute();
            System.out.println(execute4);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(new File("publisher.json"))));

        // set up authorization code flow

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER)).setDataStoreFactory(
                dataStoreFactory).build();

        String userName = System.getProperty("user.name");

        // authorize
        return new AuthorizationCodeInstalledApp(flow, new GooglePromptReceiver()).authorize(userName == null ? "localuser" : userName);
    }
}