package com.lab.emp.probe;

import com.lab.emp.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时探针 Controller，仅存在于 test 作用域。
 * 对应指导书任务 3.4：编写临时 Controller 返回 hello 验证请求链路，
 * 验证完即删除；放在 src/test 下既完成验证，又不污染正式代码。
 */
@RestController
@RequestMapping("/probe")
public class HealthProbeController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.ok("SSM 请求链路验证通过");
    }
}
