package org.codinjutsu.tools.jenkins.logic;

import java.text.Collator;
import java.util.Comparator;

public class JobNameComparator implements Comparator<String> {

    private final Collator collator;

    public JobNameComparator() {
        this.collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    public int compare(String value1, String value2) {
        return collator.compare(value1, value2);
    }
}
