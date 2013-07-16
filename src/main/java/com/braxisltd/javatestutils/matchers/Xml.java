package com.braxisltd.javatestutils.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Lists.newArrayList;

public class Xml {

    public static XmlMatcher element(String tagName) {
            return new XmlMatcher(tagName);
        }

        public static NamespaceAwareMatcher namespaceAware(String namespace) {
            return new NamespaceAwareMatcher(namespace);
        }

        public static class XmlMatcher extends TypeSafeMatcher<Element> {
            private final String tagName;
            private String text;
            private final String namespaceUri;
            private List<XmlMatcher> childMatchers = newArrayList();

            public XmlMatcher(String tagName) {
                this.tagName = tagName;
                namespaceUri = null;
            }

            public XmlMatcher(String tagName, String namespaceUri) {
                this.tagName = tagName;
                this.namespaceUri = namespaceUri;
            }

            @Override
            protected boolean matchesSafely(Element element) {
                return namespaceMatches(element) && tagNameMatches(element) && textContentMatches(element) && childrenMatch(element);
            }

            private boolean namespaceMatches(Element element) {
                return equal(element.getNamespaceURI(), namespaceUri);
            }

            private boolean childrenMatch(Element element) {
                for (XmlMatcher childMatcher : childMatchers) {
                    boolean matched = false;
                    NodeList childNodes = element.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node item = childNodes.item(i);
                        if (item instanceof Element && childMatcher.matchesSafely((Element) item)) {
                            matched = true;
                        }
                    }
                    if (!matched) {
                        return false;
                    }
                }
                return true;
            }

            private boolean textContentMatches(Element element) {
                return (text == null || element.getTextContent().equals(text));
            }

            private boolean tagNameMatches(Element element) {
                return element.getTagName().equals(tagName);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an element with name: ").appendValue(tagName);
                if (namespaceUri != null) {
                    description.appendText(" with namespace uri: ").appendValue(namespaceUri);
                }
                if (text != null) {
                    description.appendText(" with text content: ").appendValue(text);
                }
                if (!childMatchers.isEmpty()) {
                    description.appendText(" containing { ");
                    for (XmlMatcher childMatcher : childMatchers) {
                        childMatcher.describeTo(description);
                        description.appendText(" , ");
                    }
                    description.appendText(" } ");
                }
            }

            public XmlMatcher withText(String text) {
                this.text = text;
                return this;
            }

            public XmlMatcher containing(XmlMatcher matcher) {
                childMatchers.add(matcher);
                return this;
            }

        }

        public static class NamespaceAwareMatcher {
            private String namespace;

            public NamespaceAwareMatcher(String namespace) {
                this.namespace = namespace;
            }

            public XmlMatcher element(String tagName) {
                return new XmlMatcher(tagName, namespace);
            }
        }
}
