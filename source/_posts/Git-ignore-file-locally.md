---
title: Git ignore file locally
date: 2018-11-13 22:22:54
tags: [Git]
---

### 0x80 新的环境

### 0x81 Project-Level ignore

Git应该是目前来讲绝大多数开发人员都在使用VCS工具，Git本身提供分布式的仓库管理，每一个人的repo都可以视为一个仓库，通常它们只在互相同步时有local和remote的差别。

在我们的日常开发中，往往有很多文件是不属于仓库本身的，比如编译产生的临时文件和开发者自己的本地配置文件。对于这些文件，我们通常会使用GitRoot目录及子目录下的`.gitignore`文件来忽略我们不想track且untracked的文件。各大代码托管服务在线生成repo时都会提供对应类型项目的ignore模版文件，用于忽略追踪一些常见的文件或目录。

`.gitignore`设计的初衷是忽略追踪文件，有点`assume-unchanged`的意思。一个文件如果被ignore且没有被追踪，那我们无论怎样修改这个文件，Git都会忽略掉它，但是如果我们已经将这个文件添加到Stage（通过`--force`参数）或已经track了这个文件，那.gitignore将不在对该文件生效。

<!--more-->

### 0x82 Global ignore

相对于Project-Level ignore功能，和gitconfig一样，Git本身也提供全局忽略的功能，ignore文件模版与`.gitignore`文件类似，我们只需要在全局`.gitconfig`文件（通常在用户Home目录）中指向定义忽略内容的配置文件即可，比如我有一个自己的忽略规则，而这些规则不影响其他人不能将这些配置放到仓库里：

```Git
[core]
    excludesfile = /Users/fionera/.gitignore
```

### 0x83 assume-unchanged

`assume-unchanged`在前文提到过，字面意思就是假定没有发生变更，从而让Git忽略掉本次修改。比如说我有一个文件需要修改，但是这个文件是被track的，并且这个文件我不想提交从而影响其他人使用，那我可以通过如下命令假定文件没有发生变更：

```Bash
git update-index --assume-unchanged file
```

经过这次操作，Git会认为该文件没有本次发生过变更，如果想取消使用`--no-assume-unchanged`参数即可。如果我认为这个文件只在我本地会修改且不能提交，正常来讲我应该ignore掉这个文件，而`.gitignore`文件是所有人共用的，我们不能修改这个文件来达到我们的目的。因此我们需要本地的ignore配置来做这件事`GitRoot/.git/info/exclude`，与上文的全局ignore作用类似，默认情况下它忽略的是untracked的文件，配合`assume-unchanged`我们就可以模拟出本地忽略一个已经被追踪的文件的修改。

我们应如何获取哪些文件被我们做了这种处理呢？Git提供了展列各种状态文件的能力，通过如下命令就能检出：

```Bash
git ls-files -t | grep '^h'
```

有兴趣可以研究下Git ls-files提供的丰富的参数，其实用的不多。看起来一切很完美，那这样真的能忽略仓库里已经追踪的文件吗？答案是否定的，这也是为什么我用了“本次修改”，其实这种假定变更，一旦HEAD发生变更，`assume-unchanged`的文件通常会回到变更之前的样子来保证文件真的没有发生变更。对于这种需求，我们应该使用`--skip-worktree`。

### 0x84 skip-worktree

`skip-worktree`的功能能真正的达到我们的需求，它跳过了worktree对文件的跟踪，它的文件真的变了，这也意味着更新代码也能保持该文件的修改内容，因为拉取代码也会逃过该文件，更意味着当我们使用`--no-skip-worktree`来恢复跟踪，我们的修改可以被捕捉到，我们可以提交它来做永久的变更

与`assume-unchanged`类似，我们可以使用如下命令检出：

```Bash
git ls-files -v | grep '^S'
```

对于`assume-unchanged`和`skip-worktree`的更多差异以及什么场景下使用哪一种方式，可以参考这个[Summary](https://fallengamer.livejournal.com/93321.html)，前者性能更高，针对一些特殊的场景。
