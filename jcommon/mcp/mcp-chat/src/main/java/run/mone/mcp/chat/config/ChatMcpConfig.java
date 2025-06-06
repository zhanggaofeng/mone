package run.mone.mcp.chat.config;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.mcp.function.ChatFunction;
import run.mone.hive.mcp.grpc.transport.GrpcServerTransport;
import run.mone.hive.mcp.service.HiveManagerService;
import run.mone.hive.mcp.service.RoleService;
import run.mone.hive.mcp.spec.McpSchema;
import run.mone.hive.roles.tool.AskTool;
import run.mone.hive.roles.tool.AttemptCompletionTool;
import run.mone.hive.roles.tool.ChatTool;
import run.mone.hive.roles.tool.SpeechToTextTool;
import run.mone.hive.roles.tool.TextToSpeechTool;
import run.mone.mcp.chat.tool.DocumentProcessingTool;
import run.mone.mcp.chat.tool.SystemInfoTool;

import javax.annotation.Resource;


@Configuration
public class ChatMcpConfig {

    @Value("${mcp.grpc.port:9999}")
    private int grpcPort;

    @Resource
    private HiveManagerService hiveManagerService;

    @Bean
    LLM llm() {
//        LLMConfig config = LLMConfig.builder()
//                .llmProvider(LLMProvider.CLAUDE_COMPANY)
//                .url(getClaudeUrl())
//                .version(getClaudeVersion())
//                .maxTokens(getClaudeMaxToekns())
//                .build();
//        LLMConfig config = LLMConfig.builder().llmProvider(LLMProvider.OPENROUTER).build();
//        LLMConfig config = LLMConfig.builder().llmProvider(LLMProvider.DEEPSEEK).build();
//        return new LLM(config);

        LLMConfig config = LLMConfig.builder().llmProvider(LLMProvider.GOOGLE_2).build();
        config.setUrl(System.getenv("GOOGLE_AI_GATEWAY") + "streamGenerateContent?alt=sse");
        return new LLM(config);
    }

    @Bean
    @ConditionalOnProperty(name = "mcp.transport.type", havingValue = "grpc")
    GrpcServerTransport grpcServerTransport() {
        GrpcServerTransport transport = new GrpcServerTransport(grpcPort);
        transport.setOpenAuth(true);
        return transport;
    }


    @Bean
    RoleService roleService(LLM llm) {
        return new RoleService(llm,
                Lists.newArrayList(
                        new ChatTool(),
                        new AskTool(),
                        new AttemptCompletionTool(),
                        new SpeechToTextTool(),
                        new TextToSpeechTool(),
                        new DocumentProcessingTool(),
                        new SystemInfoTool()),
                Lists.newArrayList(
                        new McpSchema.Tool(ChatFunction.getName(), ChatFunction.getDesc("minzai"), ChatFunction.getToolScheme())
                ),
                hiveManagerService);
    }

    @Bean
    ChatFunction chatFunction(RoleService roleService) {
        return new ChatFunction(roleService);
    }

}