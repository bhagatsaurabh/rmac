<template>
  <main class="launch">
    <section class="banner">
      <Logo alt="RMAC logo" :config="{ maxHeight: '10rem' }" name="rmac-logo-combined" animated />
      <h1><pre>C o n s o l e</pre></h1>
    </section>
    <section class="links">
      <ExternalLink to="https://github.com/saurabh-prosoft/rmac">
        <template #prefix>
          <Icon alt="GitHub icon" name="icons/github" adaptive></Icon>
        </template>
        GitHub
      </ExternalLink>
      <ExternalLink to="https://github.com/saurabh-prosoft/rmac">
        <template #prefix>
          <Icon alt="Help icon" name="icons/help" adaptive></Icon>
        </template>
        Help
      </ExternalLink>
    </section>
    <section class="input">
      <Info class="infocon">
        <template #title>Server URL</template>
        <template #desc>The RMAC Server through which all your hosts register.</template>
      </Info>
      <input type="text" name="rmac-server-url" placeholder="my-rmac-server.com" required />
    </section>
  </main>
</template>

<script setup>
import ExternalLink from '@/components/Common/ExternalLink.vue';
import Icon from '@/components/Common/Icon.vue';
import Info from '@/components/Common/Info.vue';
import Logo from '@/components/Common/Logo.vue';
</script>

<style scoped>
.launch {
  display: flex;
  justify-content: center;
  flex-direction: column;
  height: calc(100vh - 3.5rem - 1px);
  text-align: center;
}
.launch section:not(:last-child) {
  margin-bottom: 1.5rem;
}
.launch .links a {
  margin: 0.5rem 0.5rem;
}
.launch h1 pre {
  font-family: Arial, Helvetica, sans-serif;
  color: rgb(120 120 120);
}
.launch .banner {
  margin-bottom: 2rem !important;
}

.input label {
  display: block;
}
.input input {
  width: 60vw;
  text-align: center;
  font-size: 1.1rem;
  padding: 0.3rem 1rem;
  border-radius: 999px;
  border: 1px solid grey;
  margin-top: 0.5rem;
  box-shadow: none;
  opacity: 0.8;
  transition: box-shadow var(--fx-transition-duration) linear,
    border var(--fx-transition-duration) linear, opacity var(--fx-transition-duration) linear;
}

.input input:focus {
  opacity: 1;
  outline: none;
  box-shadow: 4px 4px 10px -2px darkgrey;
}

.input input:focus {
  outline: grey;
}
.infocon {
  display: inline-block;
}

@media (min-width: 768px) {
}

@media (min-width: 1024px) {
}
</style>
