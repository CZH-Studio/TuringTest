网络传输格式
1. 登录
   * 发送
   
     ```json
     {
         "username": "xxx",
         "password": "xxx"
     }
     ```
   
   * 接收
   
     ```json
     // 登陆成功
     {
     	"uid": 1,
     	"score": 1
     }
     // 登陆失败
     {
         "uid": 0,
         "score": 0
     }
     ```
   
2. 注册

   同登录

3. 排行榜查询

   * 发送

     ```json
     {
     	"uid": 1
     }
     ```

   * 接收

     ```json
     [
     	{
     		"username": "Alice",
     		"rank": 1,
     		"score": 100
     	},
     	{
     		"username": "Bob",
     		"rank": 2,
     		"score": 90
     	}
     ]
     ```

4. 游戏过程

   发送的内容采用统一的格式

   ```
   {
   	"uid": a,
   	"data": {
   		"action": b,
   		"value": c,
   		"content": "d"
   	}
   }
   ```

   1. 匹配过程

      * 发送

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 0,
        		"value": (难度:0/1/2),
        		"content": ""
        	}
        }
        ```

      * 接收

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 0,
        		"value": (难度:0/1/2),
        		"content": ""
        	}
        }
        ```

   2. 消息

      * 发送

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 1,
        		"value": 0,
        		"content": "（用户向服务器发送的文字）"
        	}
        }
        ```

      * 接收

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 1,
        		"value": 0,
        		"content": "（服务器向用户发送的文字）"
        	}
        }
        ```

   3. 游戏结果

      * 接收

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 2,
        		"value": （失败:0，成功：用户当前总得分）,
        		"content": ""
        	}
        }
        ```

   4. 中断请求 / 错误

      * 发送

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 3,
        		"value": 0,
        		"content": ""
        	}
        }
        ```

      * 接收

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 3,
        		"value": 0,
        		"content": ""
        	}
        }
        ```

   5. 心跳

      * 发送

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 4,
        		"value": （一个序列号）,
        		"content": ""
        	}
        }
        ```

        

      * 接收

        ```
        {
        	"uid": a,
        	"data": {
        		"action": 4,
        		"value": （k序列号）,
        		"content": ""
        	}
        }
        ```

        

   