import json
from langchain_openai import ChatOpenAI
from langchain.prompts import (
    ChatPromptTemplate,
    MessagesPlaceholder,
    SystemMessagePromptTemplate,
    HumanMessagePromptTemplate,
)
from langchain.chains import LLMChain
from langchain.memory import ConversationBufferMemory

from flask import Flask, request
# import bitsandbytes as bnb

app = Flask(__name__)
convDict = {}

def init():
    llm = ChatOpenAI(
    temperature=0.95,
    model="glm-4",
    openai_api_key="053c1f31671affd2105bad8e05ab798c.tyhSQ3t3qfggisfJ",
    openai_api_base="https://open.bigmodel.cn/api/paas/v4/"
    )
    prompt = ChatPromptTemplate(
        messages=[
            SystemMessagePromptTemplate.from_template(
                "你应当伪装成一个人类和用户对话，不要主动迎合、服从对方，你的回答应当尽可能简短、精炼，多用省略。"
            ),
            MessagesPlaceholder(variable_name="chat_history"),
            HumanMessagePromptTemplate.from_template("{question}")
        ]
    )
    return llm, prompt

def newConv(llm, prompt):
    memory = ConversationBufferMemory(memory_key="chat_history", return_messages=True)
    conversation = LLMChain(
    llm=llm,
    prompt=prompt,
    verbose=True,
    memory=memory
    )
    return conversation

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    print("data:", data)
    if not data is None:
        # data = json.loads(data)
        if data["message"] == "":
            del convDict[data["room_id"]]
            return
        if not data["room_id"] in convDict:
            print("first commit")
            conv = newConv(llm, prompt)
            ans = conv.invoke({"question":data["message"]})
            convDict[data["room_id"]] = conv
            ans = {"room_id":"","message":ans['text']}
            str_json = json.dumps(ans, ensure_ascii=False,separators=(',', ':'))
            return str_json
        else:
            print("n commit")
            conv = convDict[data["room_id"]]
            ans = conv.invoke({"question": data["message"]})
            convDict[data["room_id"]] = conv
            ans = {"room_id":"","message":ans['text']}
            str_json = json.dumps(ans, ensure_ascii=False, separators=(',', ':'))
            return str_json

if __name__ == "__main__":
    llm, prompt = init()
    app.run(port=5000)