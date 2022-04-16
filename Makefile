sources := $(shell find -name '*.java')
main := com.gigamonkeys.dungeon.Dungeon
resources :=

all: build

build: compile resources

compile: $(sources)
	javac -Xdiags:verbose -Xlint:deprecation -Xlint:unchecked -cp src:classes -d classes/ $(sources)

pretty:
	prettier --plugin ~/node_modules/prettier-plugin-java/ --write **/*.java

resources: $(resources)

run:
	java -cp classes $(main)

test:
	java -cp classes com.gigamonkeys.dungeon.Test

clean:
	rm -rf classes

tidy:
	find . -name '*~' -delete
