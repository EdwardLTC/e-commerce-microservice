import androidx.compose.ui.window.ComposeUIViewController
import org.edward.app.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
