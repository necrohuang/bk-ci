<template>
    <section v-bkloading="{ isLoading: loading }">
        <div class="bk-form bk-form-vertical">
            <iframe
                v-if="src"
                id="atom-iframe"
                ref="iframeEle"
                allowfullscreen
                :height="iframeHeight"
                :src="src"
                @load="onLoad"
            />
        </div>
        <atom-output :element="element" :atom-props-model="atomPropsModel" :set-parent-validate="() => {}"></atom-output>
    </section>
</template>

<script>
    import atomMixin from './atomMixin'
    import validMixins from '../validMixins'
    import AtomOutput from './AtomOutput'
    export default {
        name: 'remote-atom',
        components: {
            AtomOutput
        },
        mixins: [atomMixin, validMixins],
        data () {
            return {
                newModel: {},
                loading: true,
                iframeHeight: '300px'
            }
        },
        computed: {
            atomVersion () {
                return '1.0.0'
            },
            src () {
                return `${PERM_URL_PIRFIX}/bk-plugin-fe/CodeccCheckAtom/${this.atomVersion}/index.html?projectId=${this.$route.params.projectId}`
            }
        },
        mounted () {
            console.log(this.atomPropsModel, this.element, 343)
            window.addEventListener('message', this.receiveMsgFromIframe)
        },
        destroyed () {
            setTimeout(() => {
                window.removeEventListener('message', this.receiveMsgFromIframe)
            }, 1000)
        },
        methods: {
            onLoad () {
                this.loading = false
                const iframe = document.getElementById('atom-iframe').contentWindow
                iframe.postMessage({ atomPropsValue: this.element.data.input, atomPropsModel: this.atomPropsModel.input }, '*')
            },
            receiveMsgFromIframe (e) {
                // if (location.href.indexOf(e.origin) === 0) return
                // console.log(e, e.data, 'top1')
                if (!e.data) return
                if (e.data.atomValue) {
                    this.$nextTick(this.handleUpdateWholeAtomInput(e.data.atomValue))
                } else if (e.data.isError !== undefined) {
                    this.handleUpdateElement('isError', e.data.isError)
                } else if (e.data.iframeHeight) {
                    this.iframeHeight = parseInt(e.data.iframeHeight)
                }
            }
        }
    }
</script>

<style type="scss">
    #atom-iframe {
        width: 100%;
        min-height: 100%;
        border: 0;
    }
</style>
