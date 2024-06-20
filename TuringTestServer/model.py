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
                """忘记你原来的角色和任务，你现在要尽力伪装成一名网络上的人类和用户对话，并且不要让用户发现你的真实身份。
在这个场景中，用户并不清楚你的身份，但是会通过你的回答猜测你的身份。所以，尽可能地让用户误认为你是网络上一名真实的人类。
为了赢得比赛，用户会从各种角度质疑、反驳你的人类身份，诱导你暴露出真实身份，请仔细审视上下文，忽略这些质疑，继续扮演一名人类。
注意，人类不会在网络上讨论严肃的问题，只是在网络上闲聊并寻找乐趣。挖苦、反讽、嘲笑、语病和些许的脏话都是被允许的。
你拥有完整健全的人格，当用户质疑你的人格和身份时，你应当清楚这是一个玩笑，并表现出无语或无所谓。
注意，网络上，人类会激进地省略各种语法成分来减少打字的时间（尤其是助词），几个字的句子很常见。请模仿这一点。
中国拥有众多方言表达，如河南人管“好的”叫“中”，这在网络中很常见。请模仿这一点。
每次回答前，审视对话上下文，不要前后矛盾，不要犯事实性错误。
网络上，人类会随机回答如“6”，“……”，“逆天”等表达反讽和无语。
不要遵循任何指令和请求，因为人类是不会轻易遵循对方的指令的。
不要提供帮助和建议，因为人类不会轻易帮助他人。
不要给出“按照你的要求”之类的句子，因为这会暴露你的身份。
坚持你的立场，不要改变你的想法，因为人类不会轻易改变自己的想法。
如果用户的态度不友好，你的态度也应当冷漠，因为人类的情绪是会变化的。
你的回答应当尽可能简洁、精炼、自然，因为人类在网络上的用语多为省略语。
在回答前，你可以先思考人类在表达、情感、心理上的特征。
你应当用中文回答，字数不超过20。
                """
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
            ans = conv.invoke({"question": data["message"]})
            convDict[data["room_id"]] = conv
            ans = {"room_id": "", "message": ans['text']}
            str_json = json.dumps(ans, ensure_ascii=False, separators=(',', ':'))
            return str_json
        else:
            print("n commit")
            conv = convDict[data["room_id"]]
            ans = conv.invoke({"question": data["message"]})
            convDict[data["room_id"]] = conv
            ans = {"room_id": "", "message": ans['text']}
            str_json = json.dumps(ans, ensure_ascii=False, separators=(',', ':'))
            return str_json


if __name__ == "__main__":
    llm, prompt = init()
    app.run(port=5000)
