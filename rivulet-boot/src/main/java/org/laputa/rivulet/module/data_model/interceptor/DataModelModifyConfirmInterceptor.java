package org.laputa.rivulet.module.data_model.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.access_limit.annotation.AccessLimit;
import org.laputa.rivulet.access_limit.annotation.LimitTimeUnit;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.mvc.interceptor.RvInterceptor;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.module.data_model.service.BuiltInDataModelService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author JQH
 * @since 下午 5:48 22/10/22
 */
@Component
@Slf4j
public class DataModelModifyConfirmInterceptor extends RvInterceptor {
    @Resource
    private AppState appState;
    @Resource
    private BuiltInDataModelService builtInDataModelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> getPathPatterns() {
        return List.of("/**");
    }

    @Override
    public List<String> getExcludePathPatterns() {
        return Arrays.asList("/api/builtInDataModel/confirmUpdateSql", "/**/*.js", "/**/*.html", "/**/*.css", "/**/*.jpg", "/**/*.png");
    }

    @SneakyThrows
    @Override
    @AccessLimit(times = 40, duration = 10, unit = LimitTimeUnit.SECOND)
    public boolean preHandle(@NotNull HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (appState.isBuiltInDataModelSynced()) return true;
        builtInDataModelService.refreshStructureUpdateSql();
        // 执行getStructureUpdateSql会再进行一次对比，有可能已经同步完成
        if (appState.isBuiltInDataModelSynced()) return true;
        String currentStructureUpdateSql = builtInDataModelService.getCurrentStructureUpdateSql();
        Result<String> updateStructureResult = Result.fail(String.class, "requireConfirmUpdateSql", "需要确认内部数据模型更新的SQL");
        updateStructureResult.setPayload(currentStructureUpdateSql);
        writeResponse(response, updateStructureResult);
        return false;
    }

    private void writeResponse(@NotNull HttpServletResponse response, Result<String> result) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            String str = objectMapper.writeValueAsString(result);
            response.getWriter().write(str);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
}
