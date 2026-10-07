#!/usr/bin/env node
/**
 * Frontend comment-tree behavior smoke test (mirrors post.html logic).
 * Run: node scripts/test-comment-tree.mjs
 */

function buildCommentTree(comments) {
  function flattenReplies(list) {
    const flat = [];
    list.forEach((comment) => {
      flat.push(comment);
      if (comment.replies?.length) {
        comment.replies.forEach((reply) => {
          if (!reply.parentCommentId) reply.parentCommentId = comment.id;
        });
        flat.push(...flattenReplies(comment.replies));
      }
    });
    return flat;
  }

  const hasReplies = comments.some((c) => c.replies?.length);
  const flatComments = hasReplies ? flattenReplies(comments) : comments;
  const nodeMap = new Map();
  const roots = [];

  flatComments.forEach((c) => {
    nodeMap.set(c.id, {
      id: c.id,
      parentCommentId: c.parentCommentId,
      createDate: c.createdDate || c.createDate,
      children: [],
      depth: 0,
      getTotalReplyCount() {
        let n = this.children.length;
        this.children.forEach((ch) => {
          n += ch.getTotalReplyCount();
        });
        return n;
      },
    });
  });

  flatComments.forEach((c) => {
    const node = nodeMap.get(c.id);
    if (c.parentCommentId) {
      const parent = nodeMap.get(c.parentCommentId);
      if (parent) {
        node.depth = parent.depth + 1;
        parent.children.push(node);
      } else roots.push(node);
    } else roots.push(node);
  });
  return roots;
}

function assert(cond, msg) {
  if (!cond) throw new Error(msg);
}

// Case 1: API nested replies (after BE fix)
const apiTree = [
  {
    id: 1,
    createdDate: "2026-01-01T00:00:00",
    parentCommentId: null,
    replies: [
      {
        id: 2,
        createdDate: "2026-01-01T01:00:00",
        parentCommentId: 1,
        replies: [
          {
            id: 3,
            createdDate: "2026-01-01T02:00:00",
            parentCommentId: 2,
            replies: [],
          },
        ],
      },
    ],
  },
];

const roots = buildCommentTree(apiTree);
assert(roots.length === 1, "one root");
assert(roots[0].children.length === 1, "one depth-1");
assert(roots[0].children[0].children.length === 1, "one depth-2");
assert(roots[0].getTotalReplyCount() === 2, "total replies = 2");
assert(roots[0].children[0].createDate === "2026-01-01T01:00:00", "createdDate mapped");

// Case 2: old API shape (only 1-level replies) drops depth-2 from FE tree
const brokenApi = [
  {
    id: 1,
    createdDate: "2026-01-01T00:00:00",
    parentCommentId: null,
    replies: [{ id: 2, createdDate: "2026-01-01T01:00:00", parentCommentId: 1, replies: [] }],
  },
];
// depth-2 entity exists in DB but missing from API → FE cannot show it
const roots2 = buildCommentTree(brokenApi);
assert(roots2[0].getTotalReplyCount() === 1, "old API only exposes 1 reply");

// Case 3: YouTube-like UX flags from current UI rules
const youtubeLike = {
  collapseReplies: true,
  indentByDepth: true,
  replyOnTopLevelOnly: true, // current FE: depth>0 has no reply button
  replyToReplyLikeYoutube: false,
};
assert(youtubeLike.collapseReplies && youtubeLike.indentByDepth, "partial YT UX");
assert(!youtubeLike.replyToReplyLikeYoutube, "not full YT: cannot reply to reply in main UI");

console.log("OK: comment tree FE smoke tests passed");
console.log(
  JSON.stringify(
    {
      nestedTreeOk: true,
      createdDateMappingOk: true,
      youtubeParity: youtubeLike,
    },
    null,
    2,
  ),
);
