import torch
import json
import sys
from transformers import AutoModelForCausalLM, AutoTokenizer


class ChatGLM:
    def __init__(self):
        self.device = "cuda"
        self.tokenizer = AutoTokenizer.from_pretrained("glm-4-9b-chat", trust_remote_code=True)
       
        self.model = AutoModelForCausalLM.from_pretrained(
            "glm-4-9b-chat",
            torch_dtype=torch.bfloat16,
            low_cpu_mem_usage=True,
            trust_remote_code=True
        ).to(self.device).eval()

        self.gen_kwargs = {"max_length": 2500, "do_sample": True, "top_k": 1}

    def chat(self, data):
        self.chat_with_glm(data)

    def chat_with_glm(self, data):
        # 调用GLM模型进行对话
        inputs = self.tokenizer.apply_chat_template(data,
                                       add_generation_prompt=True,
                                       tokenize=True,
                                       return_tensors="pt",
                                       return_dict=True,
                                       padding=True,
                                       truncation=True
                                       )
        inputs = inputs.to(self.device)
        with torch.no_grad():
            outputs = self.model.generate(**inputs, **self.gen_kwargs)
            outputs = outputs[:, inputs['input_ids'].shape[1]:]
        ansList = []
        for output in outputs:
            ans = self.tokenizer.decode(output, skip_special_tokens=True)
            ansList.append({"role":"assistant", "content": ans})
        str_json = json.dumps(ansList, ensure_ascii=False, indent=2)
        print(str_json)
        # return data

 
if __name__ == "__main__":
    chatglm = ChatGLM()
    while True:
        data = sys.argv[1]
        data = json.loads(data)
        chatglm.chat(data)






