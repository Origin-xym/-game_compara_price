你好！根据你提供的文件，我为你详细解释 `security` 文件夹及相关认证服务中每个代码文件的作用。该项目是一个基于 Java Spring Boot 的后端服务，采用了 JWT（JSON Web Token）进行无状态身份认证，并通过 ThreadLocal 实现请求级别的用户上下文管理。

以下是各文件的详细作用及逻辑解析：

### 1. AuthService.java - 认证服务接口
文件位置：[AuthService.java](backend/src/main/java/com/gameprice/user/service/AuthService.java)

该文件定义了系统认证模块的核心业务接口，规范了用户认证生命周期的各项操作，不包含具体实现逻辑。其主要作用如下：
*   **注册与登录**：定义了 `register` 和 `login` 方法，接收前端请求并返回包含登录凭证的对象。从注释可以看出，注册成功后会直接签发 Token 实现立即登录，登录时包含防爆破机制并设定了 120 分钟的会话有效期。
*   **登出**：定义了 `logout` 方法，接收 Token 字符串，暗示底层会将该 Token 加入黑名单以使其提前失效。
*   **获取当前用户**：定义了 `me` 方法，用于获取当前已登录用户的信息，供前端在状态切换后重新拉取数据。

### 2. JwtUtil.java - JWT 工具类
文件位置：[JwtUtil.java](backend/src/main/java/com/gameprice/security/JwtUtil.java)

这是安全模块的底层核心工具类，被 Spring 容器管理（`@Component`），专门负责 JWT 的生成、解析与异常处理。
*   **密钥与配置初始化**：在构造函数中读取配置文件中的密钥和过期时间。使用 `Keys.hmacShaKeyFor` 生成符合 HS256 算法安全要求的密钥。
*   **生成令牌**：通过 `generate` 方法生成 JWT。它内部生成唯一的 JTI（JWT ID），将用户 ID 设为主题，并塞入用户名和角色等自定义声明，最后计算过期时间并使用密钥签名，返回一个 `TokenInfo` 对象。
*   **解析与校验**：通过 `parse` 方法验证 Token 的签名并提取载荷。如果解析失败会抛出异常，同时提供了 `isExpiredException` 方法，方便上层调用方区分“Token 已过期”和“非法 Token”两种不同的异常情况。

### 3. LoginUser.java - 登录用户快照
文件位置：[LoginUser.java](backend/src/main/java/com/gameprice/security/LoginUser.java)

这是一个不可变的 POJO（Plain Ordinary Java Object）类，用于在系统内部传递当前登录用户的身份信息。
*   **封装用户核心字段**：包含 `userId`、`username` 和 `role` 三个使用 `final` 修饰的属性，确保用户信息在请求处理过程中不被篡改。
*   **角色判断**：提供了一个便捷的 `isAdmin()` 方法，通过判断角色值是否为 2 来快速识别当前用户是否具备管理员权限，便于后续业务逻辑做权限校验。

### 4. TokenInfo.java - 签发结果封装
文件位置：[TokenInfo.java](backend/src/main/java/com/gameprice/security/TokenInfo.java)

该文件是一个数据传输对象（DTO），用于封装 JWT 生成后的结果。
*   **包含令牌元数据**：除了包含实际的 Token 字符串外，还携带了 `jti`（令牌唯一标识符）和 `expiresAt`（过期时间）。这些信息对于实现如“主动登出（加入黑名单）”或“前端处理 Token 刷新逻辑”等高级功能至关重要。

### 5. UserContext.java - 线程级用户上下文
文件位置：[UserContext.java](backend/src/main/java/com/gameprice/security/UserContext.java)

这是一个基于 `ThreadLocal` 的工具类，用于在同一个 HTTP 请求的生命周期内共享当前登录用户的信息。
*   **线程隔离存储**：使用静态的 `ThreadLocal<LoginUser>` 变量作为持有者，确保多线程并发请求时，各个线程的用户信息互不干扰。
*   **安全获取与校验**：提供了 `require` 和 `requireUserId` 方法。当业务代码需要获取当前登录用户时，调用这些方法会从 ThreadLocal 中取出数据；如果取不到（说明拦截器未拦截到未登录请求或发生异常），会直接抛出 `UNAUTHORIZED` 业务异常。
*   **防止内存泄漏**：提供了 `clear` 方法，要求在请求结束时（通常在拦截器的 `afterCompletion` 阶段）必须调用，以清理当前线程的 ThreadLocal 变量，防止线程池复用导致的内存泄漏和用户身份串用问题。

**总结**：在这个项目中，当用户登录时，`AuthService` 会调用 `JwtUtil` 生成令牌并返回包含 `TokenInfo` 的视图对象。后续请求携带 Token 到达时，系统过滤器解析 Token 并构建 `LoginUser` 对象，随后通过 `UserContext.set()` 挂载到当前线程上，业务层则通过 `UserContext.require()` 安全地获取当前操作者的身份信息。