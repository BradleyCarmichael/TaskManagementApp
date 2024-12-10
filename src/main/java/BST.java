public class BST {
    private Node root;

    private static class Node {
        String title;
        Node left, right;

        Node(String title) {
            this.title = title;
        }
    }

    // Insert a task title into the BST
    public void insert(String title) {
        root = insertRec(root, title);
    }

    private Node insertRec(Node root, String title) {
        if (root == null) {
            root = new Node(title);
            return root;
        }
        if (title.compareTo(root.title) < 0) {
            root.left = insertRec(root.left, title);
        } else if (title.compareTo(root.title) > 0) {
            root.right = insertRec(root.right, title);
        }
        return root;
    }

    // Check if a task title exists in the BST
    public boolean contains(String title) {
        return containsRec(root, title);
    }

    private boolean containsRec(Node root, String title) {
        if (root == null) {
            return false;
        }
        if (root.title.equals(title)) {
            return true;
        }
        if (title.compareTo(root.title) < 0) {
            return containsRec(root.left, title);
        } else {
            return containsRec(root.right, title);
        }
    }
}
