package io.jenkins.plugins.forensics.util;

import org.junit.jupiter.api.Test;
import edu.hm.hafner.echarts.ItemStyle;
import static io.jenkins.plugins.forensics.assertions.Assertions.*;

class TreeMapNodeTest {

    @Test
    void shouldCreateNodeBase() {
        //Given
        TreeMapNode root = new TreeMapNode("Root");

        //When Then
        assertThat(root).hasName("Root");
        assertThat(root).hasValue(0.0);
        assertThat(root).hasNoChildren();
        assertThat(root.getItemStyle())
                .satisfies(itemStyle -> assertThat(itemStyle.getColor())
                        .isEqualTo("-"));
    }
    @Test
    void shouldCreateNodeWithValue(){
        //Given
        TreeMapNode root = new TreeMapNode("Root", 5.0);

        //When Then
        assertThat(root).hasName("Root");
        assertThat(root).hasValue(5.0);
        assertThat(root).hasNoChildren();
        assertThat(root.getItemStyle())
                .satisfies(itemStyle -> assertThat(itemStyle.getColor())
                        .isEqualTo("-"));
    }

    @Test
    void shouldCreateNodeWithValueAndColor(){
        //Given
        TreeMapNode root = new TreeMapNode("Root", "Blue", 5.0);

        //When Then
        assertThat(root).hasName("Root");
        assertThat(root).hasValue(5.0);
        assertThat(root).hasNoChildren();
        assertThat(root.getItemStyle())
                .satisfies(itemStyle -> assertThat(itemStyle.getColor())
                        .isEqualTo("Blue"));
    }

    @Test
    void shouldInsertNode(){
        //Given
        TreeMapNode root = new TreeMapNode("Root", "Blue", 5.0);
        TreeMapNode childOne = new TreeMapNode("Child_01");

        //When
        root.insertNode(childOne);

        // Then
        assertThat(root).hasChildren(childOne);

    }

}