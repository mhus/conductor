package de.mhus.con.plugin;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.mhus.con.api.AMojo;
import de.mhus.con.api.Context;
import de.mhus.con.api.ExecutePlugin;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.summerclouds.common.core.error.InternalException;
import org.summerclouds.common.core.log.MLog;
import org.summerclouds.common.core.tool.MFile;
import org.summerclouds.common.core.tool.MJson;
import org.summerclouds.common.core.tool.MString;

import java.io.File;

// https://docs.github.com/en/rest/reference/repos

@AMojo(name = "github.release",target = "github.release")
public class GitHubReleaseMojo extends MLog implements ExecutePlugin {

    @Override
    public boolean execute(Context context) throws Exception {
        
        File dir = context.getProject().getRootDir();
        
        String gitUrl = context.getProject().getProperties().getString("gitUrl", null);
        String tag = context.getProject().getProperties().getString("tag");
        String name = context.getProject().getProperties().getString("name", tag);
        
        if (gitUrl == null) {
            log().w("gitUrl not set, skip"); // or error?
            return false;
        }
        if (!dir.exists() || !dir.isDirectory()) {
            log().w("project directory not found, skip");
            return false;
        }
        if (!gitUrl.startsWith("https://github.com/")) {
            log().w("project is not a github project, skip",gitUrl);
            return false;
        }
        
        // https://github.com/mhus/mhus-reactive.git
        String apiUrl = "https://api." + MString.beforeLastIndex(gitUrl.substring(8), '.') + "/releases";
        
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(apiUrl);
        request.setHeader("Accept", "application/vnd.github.v3+json");
        ObjectNode load = MJson.createObjectNode();
        load.put("tag_name", tag);
        load.put("name", name);
        request.setEntity(new StringEntity(MJson.toString(load), ContentType.APPLICATION_JSON ) );
        
        HttpResponse resp = client.execute(request);
        try {
            if (resp.getStatusLine().getStatusCode() != 201) {
                log().e("TODO", resp);
                throw new InternalException("Creation of github release failed");
            }
            
            String respContent = MFile.readFile( resp.getEntity().getContent() );
            log().i("GitHub Release created",tag);
            if (context.getConductor().isVerboseOutput())
                log().i(respContent);
        } finally {
//            HttpClientBuilder.close(resp);
        }
        return false;
    }

}
