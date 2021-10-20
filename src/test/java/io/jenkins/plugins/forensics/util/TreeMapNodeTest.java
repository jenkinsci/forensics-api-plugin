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

        // Then
        assertThat(actualName).isEqualTo("Root");

        assertThat(root.getChildren()).isEmpty();
        assertThat(root.getValue()).containsOnly(0.0);

        assertThat(root.getItemStyle()).satisfies(
                itemStyle -> assertThat(itemStyle.getColor()).isEqualTo("-")
        );
    }

    @Test
    void shouldInsertNode() {
        TreeMapNode root = new TreeMapNode("Root");
        root.insertNode(new TreeMapNode("Child"));

        assertThat(root.getChildren()).singleElement();
    }

    @Test
    void shouldCollapseChildNodes() {
        TreeMapNode root = new TreeMapNode("Root");
        TreeMapNode levelOneChild = new TreeMapNode("levelOneChild");
        TreeMapNode levelTwoChild = new TreeMapNode("levelTwoChild");
        TreeMapNode levelThreeChild = new TreeMapNode("levelThreeChild");

        levelTwoChild.insertNode(levelThreeChild);
        levelOneChild.insertNode(levelTwoChild);
        root.insertNode(levelOneChild);

        root.collapseEmptyPackages();

        assertThat(root.getChildren()).isEmpty();
        assertThat(root).hasName("Root.levelOneChild.levelTwoChild.levelThreeChild");
    }

    @Test
    void shouldHaveToString() {
        TreeMapNode root = new TreeMapNode("Root");
        assertThat(root.toString()).isEqualTo("'Root' ([0.0])");
    }
}
