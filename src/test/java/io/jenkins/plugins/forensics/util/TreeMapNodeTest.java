package io.jenkins.plugins.forensics.util;

import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.forensics.assertions.Assertions.*;

class TreeMapNodeTest {
    @Test
    void shouldCreateNode() {
        // Given
        TreeMapNode root = new TreeMapNode("Root");

        // When Then
        assertThat(root).hasName("Root")
                .hasNoChildren()
                .hasValue(0.0);
    }

    @Test
    void shouldCreateNodeOldSchool() {
        // Given
        TreeMapNode root = new TreeMapNode("Root");

        // When
        String actualName = root.getName();

        // When Then
        assertThat(actualName).isEqualTo("Root");

        assertThat(root.getChildren()).isEmpty();
        assertThat(root.getValue()).containsOnly(0.0);

        assertThat(root.getItemStyle()).satisfies(
                itemStyle -> assertThat(itemStyle.getColor()).isEqualTo("-")
        );
    }
}