import Flutter
import UIKit
import LiveChat

public class SwiftLivechatPlugin: NSObject, FlutterPlugin {
// 这里保存 LiveChat 控制器的引用
    private var chatViewController: UIViewController?
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "livechatt", binaryMessenger: registrar.messenger())
    let instance = SwiftLivechatPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)

    let factory = EmbeddedChatViewFactory(messenger: registrar.messenger())
    registrar.register(factory, withId: "embedded_chat_view")
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
    switch call.method {
        case "getPlatformVersion":
            result("iOS " + UIDevice.current.systemVersion)
        case "beginChat":
            let arguments = call.arguments as! [String:Any]

            let licenseNo = arguments["licenseNo"] as? String
            let groupId = arguments["groupId"] as? String
            let visitorName = arguments["visitorName"] as? String
            let visitorEmail = arguments["visitorEmail"] as? String
            let customParams = arguments["customParams"] as? [String:String] ?? [:]

            guard let licenseNo = licenseNo, !licenseNo.isEmpty else {
              result(FlutterError(code: "LICENSE_ERROR", message: "License number cannot be empty", details: nil))
              return
            }

            LiveChat.licenseId = licenseNo

            if let groupId = groupId {
              LiveChat.groupId = groupId
            }
            
            if let visitorName = visitorName {
              LiveChat.name = visitorName
            }
            
            if let visitorEmail = visitorEmail {
              LiveChat.email = visitorEmail
            }

            for (key, value) in customParams {
              LiveChat.setVariable(withKey: key, value: value)
            }

            LiveChat.presentChat()
            // 添加关闭按钮
            addCloseButton()
            result(nil)

        
        case "clearSession":
            LiveChat.clearSession()
            result(nil)
        case "dismissChatWindow":
            LiveChat.dismissChat()
             result(nil)

        default:
            result(FlutterMethodNotImplemented)
    }
  }
   // 添加关闭按钮
      private func addCloseButton() {
          // 获取当前显示的 window
          if let window = UIApplication.shared.windows.first(where: { $0.isKeyWindow }) {

              // 检查 LiveChat 是否已经显示，假设 LiveChat 已经展示了一个视图
              if let liveChatView = window.subviews.first(where: { $0.isKind(of: UIView.self) }) {

                  // 创建关闭按钮
                  let closeButton = UIButton(type: .system)
                  closeButton.setTitle("CloseChat", for: .normal)
                  // 设置浅蓝色背景
                  closeButton.backgroundColor = UIColor.systemBlue  // 淡蓝色背景，使用 `.withAlphaComponent(0.3)` 使其变得更浅

                  // 设置圆角
                  closeButton.layer.cornerRadius = 10  // 设置圆角半径，值可以调整以实现不同的圆角效果

                  // 设置白色标题颜色
                  closeButton.setTitleColor(.white, for: .normal)  // 设置按钮标题的颜色为白色
                  closeButton.frame = CGRect(x: 20, y: 40, width: 80, height: 30)  // 设置按钮的大小和位置
                  closeButton.addTarget(self, action: #selector(closeChat), for: .touchUpInside)
                  // 将按钮添加到 LiveChat 窗口的顶部
                  liveChatView.addSubview(closeButton)
              }
          }
      }

      // 关闭聊天窗口
      @objc private func closeChat() {
          LiveChat.dismissChat()
      }
}
