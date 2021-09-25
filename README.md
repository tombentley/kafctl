# `kafctl` A CLI for Kafka administration

kafctl is a CLI tool for the administration of Apache Kafka clusters.

It borrows the _context_ concept from `kubectl`, meaning it stores all the information is needs for interacting with 
each cluster using a name of your choosing. There is a single, persistent, _default context_ which is used by 
commands which interact with a cluster. 

It intentionally _does not_ address use cases for:

* producing to and consuming from a Kafka cluster (`kcat` does a great job for those use cases already).
* interacting with a Kafka Connect cluster (`kcctl` does a great job for those use cases already).

## Features

* Intuitive and discoverable subcommand-based syntax
* Various output formats supported (plain ASCII, JSON, YAML, CSV)
* Bash completion
* Colour output

## Build

To build an executable JAR run:

```bash
./mvnw package
```

This will produce an executable Quarkus application JAR that can be run with:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

A native executable can be built with:

```bash
./mvnw package -Pnative
```

This will use GraalVM to compile a native binary. If you don't have GraalVM installed locally the build will attempt to pull a containerised version (using `docker` or `podman`).

## Example commands

## Contexts

### Creating a context

Create a context, giving it a name and an `Admin` client properties file:

```bash
kafctl create context my-context -f my-context.properties
```

This will store a copy of the properties file, and referenced external files like keystores and truststores, so that
`my-context.properties` can subsequently be deleted and the context is still usable. 
If you have no existing contexts then first created on will be selected as the default, otherwise the default context is not changed.

**TODO:** Support TLS and the copying described

### Listing contexts

```bash
kafctl get contexts
```

### Switching contexts

```bash
kafctl use-context my-other-context
```

### Updating a context

```bash
kafctl alter context my-context -f changed-config.properties
```

### Deleting a context

```bash
kafctl delete context my-other-context
```

If `my-other-context` was the default context you'll be left without a default context.

## Brokers

**TODO**: By default getting a broker means to get it's state
```bash
kafctl get brokers
# or
kafctl get broker 0
```

You can get the broker's configs:

```bash
kafctl get broker 0 --config 
```

Or just an individual one

```bash
kafctl get broker 0 --config auto.create.topics.enable
```

You can select a different output format using `-f`:

```bash
kafctl get broker 0 -f yaml
```

## Topics

### Creating a topic

```bash
kafctl create topic
```

### Listing topics

```bash
kafctl get topics
```

**TODO** does this imply the output format depends on the subcommand 

### Describing topics

```bash
kafctl get topic my-topic my-other-topic
```

You can select a different output format using `-f`:

```bash
kafctl get topic my-topic -f csv
```

By default, the output includes the current state, you can also get the config

```bash
kafctl get topic my-topic --config
```

## Update topic config

```
kafctl alter topic my-topic --set topic.config.name=foo --add other.config.name=bar
```

## Increase partitions

The following will increase the number of partitions of topic "my-topic" to 3: 

```
kafctl alter topic my-topic --partitions 3
```

## Deleting records

The following will delete all records before offset 78484 from partition 4 of topic "my-topic":

`kafctl delete records my-topic --partition=4 --before-offset=78484`

**TODO: Change from positional to required option, since it's less ambiguous which is the offset and which the partition**

**TODO: this should prompt, overridable with a -y or ENVVAR or via a global option.**

## Deleting topics

```
kafctl delete topic my-topic my-other-topic
```

**TODO: this should prompt, overridable with a -y or ENVVAR or via a global option.**

## Wishlist

### Partitions

```
kafctl get partitions my-topic/1 my-other-topic/2 my-third-topic/*
```
 
Or is this via a `--show=partitions`/`--show=partition=12` option to `get topics`?


### Increase RF

`kafctl update topic --replicas 3`

Bonus: Integration with cruise control (as part of the context), (via plugins, e.g. context points to some external 
kafctl-cc tool which a contract in terms of CLI/input files/output files)

### CGroups

list, get, delete for cgroups

### Transaction

list, get, delete for transactions

### Deleting records

`kafctl delete records`

### Leader election

`kafctl delete records`

### Client quotas

### Interbroker throttles

`kafkactl get broker throttles`
`kafkactl update broker throttles ...`

### Partition reassignment

Basically `alter partition my-topic/1=1,2,3 my-other-topic/4=[1,2,3]`
Or `alter topic --replicas`

### ACL mgmt

### Scram-sha mgmt

### Explain

Get docs on broker and topic configs

`kafctl explain topic config`

`kafctl explain topic log-config`

`kafctl explain broker config`

`kafctl explain user config`

Or should explain be used to explain another command `kafctl explain update topic ...` and 
something like `get broker-docs`

### Drain

Somewhat similar to `kubectl drain`, this will reassign all the replicas from a given broker(s):

`kafctl drain 1`


### Clusters

It would be very neat to be able to spin up scratch clusters.
This should probably be a different tool (cf `kubectl` and `minikube`). 
Let's call it `kafcluster`.

```bash
kafcluster start --brokers 3 --controllers 1
```

This would spin up some containers and also configure the `kafctl` context to point to this new cluster.

To stop a scratch cluster:

```bash
kafcluster stop
```

And to delete:

```bash
kafcluster delete
```

### 'Proper' completion

E.g. completing topic names from those available in the cluster.

### Colour

Colourised output. In particular when connected to a TTY we could use colour to represent non-live brokers,
partitions at or below min ISR, leaderless partitions etc. This might be interesting for tabular output because it's likely to screw up the alignment code. The table API would need to understand about console colour codes. Similarly difficult for JSON (you could colour the content of a String, but not for a Number). But since JSON is more commonly used for programatic stuff that's less of a problem.

### Polling

Like `kubectl get -w`, but, because Admin doesn't have watches perhaps we'd support `kafctl get topic --poll[=2s]` instead, just dumping the output on each poll.
Perhaps support `-w` by internal diffing from the last output

### Delegation tokens

### Enabling features

Note the enabled and available features should be part of the `get cluster features` output.

### Log dirs

### `kafctl config`

Global configs for things that aren't part of a context.
For example override defaults for some options (`--dry-run` by default, or forcing colors on/off)

### `kafctl edit`

* Editing a context (.e. the admin properties file)
* Editing the global configs file (as used by `kafctl config`)

### Top level `--verbose`

### `kafctl version`

Print a version number