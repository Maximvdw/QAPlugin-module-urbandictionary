package be.maximvdw.qaplugin.modules;

import be.maximvdw.qaplugin.api.AIModule;
import be.maximvdw.qaplugin.api.AIQuestionEvent;
import be.maximvdw.qaplugin.api.QAPluginAPI;
import be.maximvdw.qaplugin.api.ai.Context;
import be.maximvdw.qaplugin.api.ai.Intent;
import be.maximvdw.qaplugin.api.ai.IntentResponse;
import be.maximvdw.qaplugin.api.ai.IntentTemplate;
import be.maximvdw.qaplugin.api.annotations.ModuleAuthor;
import be.maximvdw.qaplugin.api.annotations.ModuleDescription;
import be.maximvdw.qaplugin.api.annotations.ModuleName;
import be.maximvdw.qaplugin.api.annotations.ModuleVersion;
import be.maximvdw.qaplugin.api.exceptions.FeatureNotEnabled;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

/**
 * UrbanDictionaryModule
 * Created by maxim on 31-Dec-16.
 */
@ModuleName("UrbanDictionary")
@ModuleAuthor("Maximvdw")
@ModuleVersion("1.1.0")
@ModuleDescription("Search on urban dictionary")
public class UrbanDictionaryModule extends AIModule {
    public UrbanDictionaryModule() {
        Intent question = new Intent("QAPlugin-module-urbandictionary")
                .addTemplate(new IntentTemplate()
                        .addPart("who is ")
                        .addPart(new IntentTemplate.TemplatePart("Obama")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("what is ")
                        .addPart(new IntentTemplate.TemplatePart("trump")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("what is a ")
                        .addPart(new IntentTemplate.TemplatePart("leacher")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart("?"))
                .addTemplate(new IntentTemplate()
                        .addPart("give the definition of ")
                        .addPart(new IntentTemplate.TemplatePart("+1")
                                .withMeta("@sys.any")
                                .withAlias("query")))
                .addTemplate(new IntentTemplate()
                        .addPart("give me the definition of ")
                        .addPart(new IntentTemplate.TemplatePart("Flying horsemen")
                                .withMeta("@sys.any")
                                .withAlias("query")))
                .addTemplate(new IntentTemplate()
                        .addPart("what does ")
                        .addPart(new IntentTemplate.TemplatePart("flying")
                                .withMeta("@sys.any")
                                .withAlias("query"))
                        .addPart(" mean ?"))
                .addResponse(new IntentResponse()
                        .withAction(this)
                        .addParameter(new IntentResponse.ResponseParameter("query", "$query")
                                .withDataType("@sys.any")
                                .setRequired(true)
                                .addPrompt("Not sure what you want me to look up?")
                                .addPrompt("What do you want me to look up?")
                                .addPrompt("For what do you want to know a definition?"))
                        .addMessage(new IntentResponse.TextResponse()
                                .addSpeechText("I was unable to find that word :S")
                                .addSpeechText("I couldn't find anything about that on the interwebzzz")
                                .addSpeechText("I'm clueless here ...")
                                .addSpeechText("Not sure what that means")))
                .withPriority(Intent.Priority.LOW);

        try {
            // Upload the intents
            if (QAPluginAPI.findIntentByName(question.getName()) == null) {
                if (!QAPluginAPI.uploadIntent(question)) {
                    warning("Unable to upload intent!");
                }
            }
        } catch (FeatureNotEnabled ex) {
            severe("You do not have a developer access token in your QAPlugin config!");
        }
    }

    public String getResponse(AIQuestionEvent event) {
        if (!event.getParameters().containsKey("query")) {
            return event.getDefaultResponse();
        }
        try {
            UrbanDictionaryResult answer = getResponse(event.getParameters().get("query"));
            if (answer == null) {
                return ((IntentResponse.TextResponse) event.getDefaultResponses().get(0)).getSpeechTexts().get(0);
            } else {
                String definition = answer.getDefinition().replace("\r", "");
                if (definition.length() > 250) {
                    definition = definition.substring(0, 250) + " ...";
                }
                Context udc = new Context("urbandictionary", 1);
                addContext(udc, event.getPlayer());
                return definition;
            }
        } catch (Exception ex) {
            // Error
            return ((IntentResponse.TextResponse) event.getDefaultResponses().get(0)).getSpeechTexts().get(0);
        }
    }

    public UrbanDictionaryResult getResponse(String query) throws UnsupportedEncodingException, ParseException {
        String url = "http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(query, "UTF-8");
        String source = source(url);
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(source);
        JSONArray list = (JSONArray) object.get("list");
        int amount = list.size();
        if (amount == 0) {
            return null;
        }
        Random random = new Random();
        int idx = random.nextInt(amount);
        JSONObject response = (JSONObject) list.get(idx);
        UrbanDictionaryResult result = new UrbanDictionaryResult(response);
        return result;
    }

    public static String source(String urlSite) {
        StringBuilder result = new StringBuilder();

        URL url;
        URLConnection urlConn;

        try {
            url = new URL(urlSite);
            urlConn = url.openConnection();
            urlConn.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            Reader reader = new InputStreamReader(urlConn.getInputStream(),
                    "utf-8");
            BufferedReader br = new BufferedReader(reader);

            int byteRead;
            while ((byteRead = br.read()) != -1)
                result.append((char) byteRead);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    class UrbanDictionaryResult {
        private String definition = "";
        private int thumbsUp = 0;
        private int thumbsDown = 0;
        private String example = "";
        private String author = "";
        private String permalink = "";

        public UrbanDictionaryResult(JSONObject object) {
            definition = (String) object.get("definition");
            author = (String) object.get("author");
            example = (String) object.get("example");
            permalink = (String) object.get("permalink");
            thumbsDown = (Integer) object.get("thumbs_down");
            thumbsUp = (Integer) object.get("thumbs_up");
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public int getThumbsUp() {
            return thumbsUp;
        }

        public void setThumbsUp(int thumbsUp) {
            this.thumbsUp = thumbsUp;
        }

        public int getThumbsDown() {
            return thumbsDown;
        }

        public void setThumbsDown(int thumbsDown) {
            this.thumbsDown = thumbsDown;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPermalink() {
            return permalink;
        }

        public void setPermalink(String permalink) {
            this.permalink = permalink;
        }
    }
}
