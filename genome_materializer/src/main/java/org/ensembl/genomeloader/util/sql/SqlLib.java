package org.ensembl.genomeloader.util.sql;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

public class SqlLib {

    private final Map<String, String> queries = new HashMap<>();

    public SqlLib(String resource) {
        try {
            String text = IOUtils.toString(this.getClass().getResourceAsStream(resource), "UTF-8");
            Document doc = new Builder().build(text, null);
            Element rootElement = doc.getRootElement();
            Elements sql = rootElement.getChildElements("sql");
            for (int i = 0; i < sql.size(); i++) {
                Element child = sql.get(i);
                queries.put(child.getAttribute("name").getValue(), child.getValue());
            }

        } catch (IOException | ParsingException e) {
            throw new RuntimeException("Could not parse resource " + resource);
        }
    }

    public String getQuery(String key) {
        return queries.get(key);
    }

    public String getQuery(String key, String[] placeholders) {
        String query = queries.get(key);
        if (!StringUtils.isEmpty(query)) {
            for (int i = 0; i < placeholders.length; i++) {
                query = query.replaceAll("\\{" + i + "\\}", placeholders[i]);
            }
        }
        return query;
    }

}
