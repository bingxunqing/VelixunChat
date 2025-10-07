## 重构代码

我发现，初代代码只使用四个文件，在后续修改过程十分笨重，
且代码很多，不够清晰，于是，10.6-21:51，我打算开始重构代码
使用，接口，多态等方式让我的项目结构清晰化。

功能函数:  

服务端：login,register,getUserNumber,changeChatTarget

handleMsg,sendMessage,BroadMsg,sendMsgByUsername  

addHandler,removeHandler,canRegister,successRegister

服务端: Listener,sendMsgByClient,handlerMsgByClient  

Exit

接口:  

命令接口(login,register,broadMsg,sendMsgToSomebody,changeChatTarget):
执行

类：  

serverControl,Client.Client,ClientHandle,db.DatabaseManager,CommandParser  

login,register,PublicChat,PrivateChat,ChangeChatTarget


**于10.7-1:22完成重构**


