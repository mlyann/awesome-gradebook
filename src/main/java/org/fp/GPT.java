package org.fp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

public final class GPT {
    private static final OpenAIClient client;
    static {
        Dotenv env   = Dotenv.configure().ignoreIfMissing().load();
        client = OpenAIOkHttpClient.builder().apiKey(env.get("OPENAI_API_KEY", "")).build();
    }
    private GPT() { }// Prevent instantiation (fake method)
    public static String chat(String userPrompt) {
        ChatCompletionCreateParams p = ChatCompletionCreateParams.builder()
                .addUserMessage(userPrompt)
                .model(ChatModel.GPT_4O_MINI)
                .build();
        ChatCompletion cp = client.chat().completions().create(p);
        return cp.choices().get(0).message().content().orElse("No response");
    }
}