package org.minijpa.metadata;

import org.minijpa.jpa.db.LockTypeUtils;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamedQueryParser {

    public Optional<Map<String, MiniNamedQueryMapping>> parse(
            Class<?> c) {
        Map<String, MiniNamedQueryMapping> miniNamedQueryMap = new HashMap<>();
        NamedQuery[] namedQueryList = c.getAnnotationsByType(NamedQuery.class);
        for (NamedQuery namedQuery : namedQueryList) {
            String name = namedQuery.name();
            MiniNamedQueryMapping miniNamedQuery = parse(namedQuery);
            miniNamedQueryMap.put(name, miniNamedQuery);
        }

        NamedQueries[] namedQueryListList = c.getAnnotationsByType(NamedQueries.class);
        for (NamedQueries namedQueries : namedQueryListList) {
            NamedQuery[] namedQueryArray = namedQueries.value();
            for (NamedQuery namedQuery : namedQueryArray) {
                String name = namedQuery.name();
                MiniNamedQueryMapping miniNamedQuery = parse(namedQuery);
                miniNamedQueryMap.put(name, miniNamedQuery);
            }
        }

        return Optional.of(miniNamedQueryMap);
    }


    private MiniNamedQueryMapping parse(NamedQuery namedQuery) {
        String name = namedQuery.name();
        if (name == null || name.trim().isEmpty())
            throw new IllegalStateException("@NamedQuery name attribute is null");

        String query = namedQuery.query();
        if (query == null || query.trim().isEmpty())
            throw new IllegalStateException("@NamedQuery query attribute is null");

        MiniNamedQueryMapping miniNamedQuery = new MiniNamedQueryMapping(name, query);
        miniNamedQuery.setLockType(LockTypeUtils.toLockType(namedQuery.lockMode()));
        if (namedQuery.hints().length > 0)
            miniNamedQuery.setHints(new HashMap<>());

        for (QueryHint queryHint : namedQuery.hints()) {
            miniNamedQuery.getHints().put(queryHint.name(), queryHint.value());
        }

        return miniNamedQuery;
    }
}
