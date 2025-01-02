package uk.gov.hmcts.ccd.definition.store.repository.interceptor;

import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;

import java.io.Serializable;

@Component
public class CaseRoleInterceptor implements Interceptor, Serializable {
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof CaseRoleEntity) {
            CaseRoleEntity caseRoleEntity = (CaseRoleEntity) entity;
            caseRoleEntity.setReference(caseRoleEntity.getReference().toUpperCase());
            return true;
        }
        return false;
    }
}
