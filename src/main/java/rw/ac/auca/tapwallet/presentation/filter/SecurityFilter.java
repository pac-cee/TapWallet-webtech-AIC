package rw.ac.auca.tapwallet.presentation.filter;

import rw.ac.auca.tapwallet.domain.model.user.Role;
import rw.ac.auca.tapwallet.presentation.bean.UserSessionBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Server-side access control. Hiding a menu item is not security — every request
 * for a protected page passes through here, so typing a URL directly cannot get
 * a customer into the admin area.
 */
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String context = request.getContextPath();
        String path = request.getRequestURI().substring(context.length());

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(UserSessionBean.SESSION_KEY);
        String roleName = session == null ? null : (String) session.getAttribute(UserSessionBean.SESSION_KEY + ".role");

        if (userId == null || roleName == null) {
            response.sendRedirect(context + "/login.xhtml");
            return;
        }

        Role role = Role.valueOf(roleName);
        if (!isAllowed(path, role)) {
            response.sendRedirect(context + "/access-denied.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return path.startsWith("/login.xhtml")
                || path.startsWith("/access-denied.xhtml")
                || path.startsWith("/javax.faces.resource/")
                || path.equals("/")
                || path.isEmpty();
    }

    private boolean isAllowed(String path, Role role) {
        if (path.startsWith("/admin/")) {
            return role == Role.ADMIN;
        }
        if (path.startsWith("/customer/")) {
            return role == Role.CUSTOMER;
        }
        if (path.startsWith("/merchant/")) {
            return role == Role.MERCHANT;
        }
        return true;
    }

    @Override
    public void destroy() {
    }
}
