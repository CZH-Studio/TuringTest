// package com.example.demo.controller;
 
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.demo.dto.ChatRequestDTO;
// import com.example.demo.dto.ChatResponseVO;
// import com.example.demo.service.IAiAppService;

// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import io.swagger.annotations.ApiParam;
 
// /**
//  * AI应用Controller
//  *
//  * @author wsl
//  * @date 2024/02/19
//  */
// @RestController
// @Api(tags = "AI应用")
// @RequestMapping("/llm/middle")
// public class AiAppController {
 
//     @Autowired
//     private IAiAppService service;

//     @PostMapping("/chat-message")
//     @ApiOperation("向大模型发起对话请求")
//     public ChatResponseVO chatMessage(
//             @ApiParam(value = "模型类型(ErnieBot/SparkDesk/ChatGlm/QianWen)", required = true) @RequestParam String modelType,
//             @ApiParam(value = "消息参数", required = true) @RequestBody ChatRequestDTO dto) {
//         try {
//             return service.chatMessage(modelType, dto);
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//     }
 
// }