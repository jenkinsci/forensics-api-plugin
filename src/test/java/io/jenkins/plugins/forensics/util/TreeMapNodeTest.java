package io.jenkins.plugins.forensics.util;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

class TreeMapNodeTest {
    @Test
    void shouldCreateNode() {
        // Given
        TreeMapNode root = new TreeMapNode("Root");

        // When Then
        assertThat(root)
                .hasName("Root")
                .hasNoChildren()
                .hasValue(0.0);
    }

    @Test
    void shouldCreateNodeOldSchool() {
        // Given
        TreeMapNode root = new TreeMapNode("Root");

        // When
        String actualName = root.getName();

        // Then
        assertThat(root.getName()).isEqualTo("Root");
        assertThat(root.getChildren()).isEmpty();
        assertThat(root.getValue()).containsOnly(0.0);

        assertThat(root.getItemStyle()).satisfies(
                itemStyle -> assertThat(itemStyle.getColor()).isEqualTo("-")
        );
    }

    @Test
    void shouldEqualsNode() {
        TreeMapNode root = new TreeMapNode("Root");

        assertThat(root).isEqualTo(root);
    }

    @Test
    void shouldCreateNodeChildren() {
        TreeMapNode root = new TreeMapNode("Root");
        TreeMapNode child = new TreeMapNode("child");
        root.insertNode(child);

        assertThat(root).hasChildren(child);
    }

    @Test
    void shouldCollapseEmptyPackages() {

    }
}