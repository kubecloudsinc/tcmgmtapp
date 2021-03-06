
package com.onecloud.tcmgmt.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onecloud.tcmgmt.domain.appdb.IdentifiableEntity;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

public class IdentifiableEntityDeleteController extends AbstractIdentifiableEntityController {

    public IdentifiableEntityDeleteController() {
        super.setSupportedMethods(new String[] {
            "DELETE"
        });
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        long id = ServletRequestUtils.getRequiredLongParameter(request, "id");
        IdentifiableEntity entity = super.dao.getById(id);
        if (entity == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            super.dao.delete(entity);
            super.logger.debug("Deleted entity by id {}", id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        return null;
    }
}
