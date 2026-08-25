# Extension development registry

Project-local extensions use stable markers so their owned files and upstream integration points
remain easy to locate during review:

```text
EXTENSION DEVELOPMENT [<extension-id>] [ADDED|MODIFIED]
```

- `ADDED` marks a file created and owned by an extension.
- `MODIFIED` marks an integration point in an existing upstream file. Modified blocks use matching
  `BEGIN` and `END` markers.
- Each extension has an independent detail file in this directory so unrelated extensions can be
  reviewed and merged separately.

Locate all registered extension changes with:

```bash
rg -n "EXTENSION DEVELOPMENT \[EXT-[^]]+\]" app/src dev/extensions
```
