package com.github.wangji92.arthas.plugin.action.arthas;

import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.github.wangji92.arthas.plugin.ui.ArthasActionStaticDialog;
import com.github.wangji92.arthas.plugin.utils.NotifyUtils;
import com.github.wangji92.arthas.plugin.utils.PropertiesComponentUtils;
import com.github.wangji92.arthas.plugin.utils.StringUtils;
import com.google.common.base.Splitter;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 参考 : https://blog.csdn.net/xunjiushi9717/article/details/94050139
 * 参考 : https://github.com/alibaba/arthas/issues/641
 * 参考 : https://github.com/alibaba/arthas/issues/71
 * 参考 : https://commons.apache.org/proper/commons-ognl/language-guide.html
 * <p>
 * org.springframework.core.env.MutablePropertySources 中的优先级 addFirst高  addLast低
 * <p>
 * 下一个版本可以考虑一下添加spring 属性值，感觉这个用得比较少 https://my.oschina.net/u/2263272/blog/1824864
 *
 * @author 汪小哥
 * @date 21-01-2020
 */
public class ArthasOgnlSpringAllPropertySourceCommandAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (editor == null || project == null) {
            return;
        }

        //这里获取spring context的信息
        String springContextValue = PropertiesComponentUtils.getValue(ArthasCommandConstants.SPRING_CONTEXT_STATIC_OGNL_EXPRESSION);
        if (StringUtils.isBlank(springContextValue) || ArthasCommandConstants.DEFAULT_SPRING_CONTEXT_SETTING.equals(springContextValue)) {
            NotifyUtils.notifyMessage(project, "Static Spring context 需要手动配置，具体参考Arthas Idea help 命令获取相关文档", NotificationType.ERROR);
            return;
        }
        // 获取class的classloader
        List<String> springContextCLassLists = Splitter.on('@').omitEmptyStrings().splitToList(springContextValue);
        if (springContextCLassLists.isEmpty()) {
            NotifyUtils.notifyMessage(project, "请正确配置 Static Spring context 信息，具体参考Arthas Idea help 命令获取相关文档", NotificationType.ERROR);
        }
        String className = springContextCLassLists.get(0);

        springContextValue = ArthasCommandConstants.SPRING_CONTEXT_PARAM + "=" + springContextValue;
        if (!springContextValue.endsWith(",")) {
            springContextValue = springContextValue + ",";
        }
        //ognl -x 3 '#springContext=@applicationContextProvider@context,
        String join = String.join(" ", "ognl", "-x", ArthasCommandConstants.RESULT_X);

        String command = String.format(ArthasCommandConstants.SPRING_ALL_PROPERTY, join, springContextValue, ArthasCommandConstants.SPRING_CONTEXT_PARAM);

        new ArthasActionStaticDialog(project, className, command).open("arthas ognl spring get all property");
    }
}
