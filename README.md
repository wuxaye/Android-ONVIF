

<p align="center">
 <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/onvif-1.png" >  <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/onvif-2.png" >
</p>

<p align="center">
 <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/onvif-3.png" >  <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/onvif-4.png" >
</p>

### 海康摄像头配置
<p align="center">
 <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/hk1.png" >  <img width="45%" height="auto" src="https://github.com/wuxaye/Android-ONVIF/blob/main/img/hk2.png" >
</p>

### 注意事项
> 同一网段下onvif发现不了设备？
在安卓设备上使用 ifconfig 查看网络配置，如下为插入网线的网络信息：
```
eth0      Link encap:Ethernet  HWaddr c2:97:16:3a:a9:8c  Driver sunxi-gmac
          inet addr:192.168.1.15  Bcast:192.168.255.255  Mask:255.255.0.0 
          inet6 addr: fe80::279a:1e2f:fc39:7f0/64 Scope: Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:2136 errors:0 dropped:3 overruns:0 frame:0 
          TX packets:236 errors:0 dropped:0 overruns:0 carrier:0 
          collisions:0 txqueuelen:1000 
          RX bytes:237857 TX bytes:34357 
          Interrupt:120
```
要想摄像头被onvif发现，则首先要求摄像头和安卓设备在同一网段下，如上则为 192.168. 网段下，至于为什么不是 192.168.1，是因为 子网掩码为 255.255.0.0，只看前两位！
对应的 传入的onvif 广播地址则为 192.168.255.255，这样onvif才能发现设备！

---

在安卓设备上通过ONVIF协议搜索摄像头的完整流程可以分为以下几个关键步骤：

### 1. 前期准备
- **环境设置**：确保开发工具（如Android Studio）已安装，并在`AndroidManifest.xml`中添加必要的网络权限，如`INTERNET`和`ACCESS_NETWORK_STATE`。
- **依赖库**：引入必要的SOAP和ONVIF相关库，以便处理协议通信和设备交互。

### 2. 设备发现
- **发送Probe请求**：使用WS-Discovery协议，通过多播地址`239.255.255.250`的端口`3702`发送SOAP格式的Probe消息，主动搜索网络中的ONVIF设备。
- **接收响应**：监听多播组，接收摄像头设备返回的响应信息，包含设备的基本地址和服务信息。

### 3. 解析设备信息
- **解析响应内容**：提取响应中的设备地址（XAddrs）、设备类型和其他相关信息，为后续通信做准备。
- **记录设备信息**：将发现的设备信息存储起来，以便后续步骤使用。

### 4. 鉴权
- **准备认证信息**：获取摄像头的用户名和密码，通常由用户提供或预先配置。
- **进行身份验证**：使用HTTP Digest认证或WS-Security标准，将认证信息包含在后续的SOAP请求头中，确保与设备的通信安全。

### 5. 获取设备详细信息
- **发送GetDeviceInformation请求**：构建并发送包含认证信息的SOAP请求，获取设备的详细信息，如制造商、型号、固件版本等。
- **解析设备响应**：处理设备返回的SOAP响应，提取所需的详细信息，确保设备状态和功能正常。

### 6. 获取媒体服务信息
- **查询媒体服务**：通过发送`GetCapabilities`或`GetServices`请求，获取设备的媒体服务地址。
- **获取视频流地址**：使用媒体服务的`GetStreamUri`方法，获取摄像头的视频流地址（如RTSP链接），以便后续视频播放或录像。

### 7. 注意事项
- **网络配置**：确保安卓设备与摄像头处于同一局域网，且网络设置允许多播通信，防止防火墙阻挡探测包。
- **多线程处理**：将网络操作放在子线程中执行，避免阻塞主线程，提升应用响应速度。
- **错误处理**：妥善处理网络异常、解析错误和鉴权失败等情况，提供友好的用户提示。
- **安全性**：保护用户的认证信息，避免明文存储密码，建议使用加密存储或安全传输。
- **兼容性测试**：不同品牌和型号的ONVIF设备可能存在协议实现差异，需进行广泛测试以确保兼容性。

