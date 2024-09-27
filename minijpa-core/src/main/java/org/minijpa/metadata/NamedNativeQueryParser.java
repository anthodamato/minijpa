package org.minijpa.metadata;

import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;

import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.QueryHint;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamedNativeQueryParser {

    public Optional<Map<String, MiniNamedNativeQueryMapping>> parse(
            Class<?> c) {
        Map<String, MiniNamedNativeQueryMapping> miniNamedQueryMap = new HashMap<>();
        NamedNativeQuery[] namedQueryList = c.getAnnotationsByType(NamedNativeQuery.class);
        for (NamedNativeQuery namedQuery : namedQueryList) {
            String name = namedQuery.name();
            MiniNamedNativeQueryMapping miniNamedQuery = parse(namedQuery);
            miniNamedQueryMap.put(name, miniNamedQuery);
        }

        NamedNativeQueries[] namedQueryListList = c.getAnnotationsByType(NamedNativeQueries.class);
        for (NamedNativeQueries namedQueries : namedQueryListList) {
            NamedNativeQuery[] namedQueryArray = namedQueries.value();
            for (NamedNativeQuery namedQuery : namedQueryArray) {
                String name = namedQuery.name();
                MiniNamedNativeQueryMapping miniNamedQuery = parse(namedQuery);
                miniNamedQueryMap.put(name, miniNamedQuery);
            }
        }

        return Optional.of(miniNamedQueryMap);
    }


    private MiniNamedNativeQueryMapping parse(NamedNativeQuery namedQuery) {
        String name = namedQuery.name();
        if (name == null || name.trim().isEmpty())
            throw new IllegalStateException("@NamedNativeQuery name attribute is null");

        String query = namedQuery.query();
        if (query == null || query.trim().isEmpty())
            throw new IllegalStateException("@NamedNativeQuery query attribute is null");

        MiniNamedNativeQueryMapping miniNamedQuery = new MiniNamedNativeQueryMapping(name, query);
        if (namedQuery.hints().length > 0)
            miniNamedQuery.setHints(new HashMap<>());

        for (QueryHint queryHint : namedQuery.hints()) {
            miniNamedQuery.getHints().put(queryHint.name(), queryHint.value());
        }

        if (namedQuery.resultSetMapping() != null && !namedQuery.resultSetMapping().trim().isEmpty()) {
            miniNamedQuery.setResultSetMapping(namedQuery.resultSetMapping().trim());
        }

        if (namedQuery.resultClass() != null && namedQuery.resultClass() != void.class) {
            miniNamedQuery.setResultClass(namedQuery.resultClass());
        }

        return miniNamedQuery;
    }
}
